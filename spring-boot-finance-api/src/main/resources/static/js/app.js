(function () {
    const AUTH_KEY = "financeDashboard.auth";
    const root = document.getElementById("page-root");
    const dialog = document.getElementById("entity-dialog");
    const dialogTitle = document.getElementById("dialog-title");
    const dialogKicker = document.getElementById("dialog-kicker");
    const dialogBody = document.getElementById("dialog-body");
    const toast = document.getElementById("toast");
    const page = document.body.dataset.page || "dashboard";
    const today = new Date();
    const todayIso = toDateInput(today);
    const defaultStart = "2024-01-01";
    const state = {
        auth: readAuth(),
        user: null,
        categories: [],
        recordPage: 0,
        userPage: 0,
        auditPage: 0,
        records: [],
        users: []
    };

    boot();

    async function boot() {
        if (!state.auth || !state.auth.accessToken) {
            redirectToLogin();
            return;
        }

        bindShell();
        setToday();

        try {
            state.user = await api("/api/auth/me");
            state.auth.user = state.user;
            writeAuth(state.auth);
            hydrateShell();
            await route();
        } catch (error) {
            if (error.status === 401) {
                redirectToLogin();
                return;
            }
            renderError(error);
        }
    }

    async function route() {
        const guarded = {
            analytics: ["ANALYST", "ADMIN"],
            users: ["ADMIN"],
            audit: ["ADMIN"]
        };
        if (guarded[page] && !hasAnyRole(guarded[page])) {
            renderAccess(pageTitle(page));
            return;
        }

        if (["dashboard", "analytics", "records", "categories"].includes(page)) {
            await loadCategories();
        }

        const routes = {
            dashboard: renderDashboard,
            analytics: renderAnalytics,
            records: renderRecords,
            categories: renderCategories,
            users: renderUsers,
            audit: renderAudit,
            profile: renderProfile
        };

        await (routes[page] || renderDashboard)();
    }

    function bindShell() {
        document.getElementById("dialog-close").addEventListener("click", closeDialog);
        document.getElementById("sidebar-logout").addEventListener("click", logout);
        document.querySelector(".menu-button").addEventListener("click", () => {
            const sidebar = document.getElementById("sidebar");
            const isOpen = sidebar.classList.toggle("is-open");
            document.querySelector(".menu-button").setAttribute("aria-expanded", String(isOpen));
        });
    }

    function hydrateShell() {
        const displayName = fullName(state.user) || state.user.username;
        document.getElementById("sidebar-user").textContent = displayName;
        document.getElementById("role-chip").textContent = primaryRole();
        document.querySelectorAll("[data-role]").forEach((element) => {
            const allowed = element.dataset.role.split(/\s+/).some((role) => hasAnyRole([role]));
            element.classList.toggle("is-hidden", !allowed);
        });
    }

    function setToday() {
        document.getElementById("today-chip").textContent = formatDate(todayIso);
    }

    async function renderDashboard() {
        const start = localStorage.getItem("financeDashboard.periodStart") || defaultStart;
        const end = localStorage.getItem("financeDashboard.periodEnd") || todayIso;
        const summary = await api(`/api/dashboard/summary?${params({ startDate: start, endDate: end })}`);
        let trends = summary.monthlyTrends || [];
        if (!trends.length) {
            trends = buildMonthlyTrends((await api("/api/records?size=200&sortBy=transactionDate&sortDirection=asc")).content || []);
        }

        root.innerHTML = `
            ${pageHead("Operating balance", "A disciplined overview of income, expenses, cash position, and the records behind them.", hasAnyRole(["ADMIN"]) ? `<button class="button primary" data-action="new-record">New record</button>` : `<a class="button primary" href="/records">View records</a>`)}
            <section class="filter-panel">
                <form id="period-form" class="filter-row">
                    ${dateField("startDate", "Start", start)}
                    ${dateField("endDate", "End", end)}
                    <button class="text-button" type="submit">Refresh period</button>
                </form>
            </section>
            <section class="metric-grid">
                ${metric("Net balance", money(summary.netBalance), "featured")}
                ${metric("Income", money(summary.totalIncome))}
                ${metric("Expenses", money(summary.totalExpense))}
                ${metric("Savings rate", `${number(summary.savingsRate)}%`)}
                ${metric("Transactions", number(summary.totalTransactions))}
                ${metric("Period", `${formatDate(summary.periodStart)} to ${formatDate(summary.periodEnd)}`)}
            </section>
            <section class="asymmetric-grid">
                <div class="section">
                    ${sectionHead("Monthly movement", `<a class="text-button" href="/analytics">Open analytics</a>`)}
                    ${renderTrendGrid(trends)}
                </div>
                <div class="section">
                    ${sectionHead("Expense mix", "")}
                    ${renderBars(summary.expenseByCategory || [])}
                </div>
            </section>
            <section class="section">
                ${sectionHead("Recent transactions", `<a class="text-button" href="/records">All records</a>`)}
                ${renderRecordTable(summary.recentTransactions || [], false)}
            </section>
        `;

        document.getElementById("period-form").addEventListener("submit", async (event) => {
            event.preventDefault();
            const form = event.currentTarget;
            localStorage.setItem("financeDashboard.periodStart", form.startDate.value || defaultStart);
            localStorage.setItem("financeDashboard.periodEnd", form.endDate.value || todayIso);
            await renderDashboard();
        });
        bindRootActions();
    }

    async function renderAnalytics() {
        const start = localStorage.getItem("financeDashboard.analyticsStart") || defaultStart;
        const end = localStorage.getItem("financeDashboard.analyticsEnd") || todayIso;
        const query = params({ startDate: start, endDate: end });
        const [monthly, weekly, daily, expenses, topIncome, topExpense] = await Promise.all([
            api("/api/dashboard/analytics/monthly-trends?months=12"),
            api("/api/dashboard/analytics/weekly-trends?weeks=12"),
            api(`/api/dashboard/analytics/daily-summary?${query}`),
            api(`/api/dashboard/analytics/category-breakdown?${params({ type: "EXPENSE", startDate: start, endDate: end })}`),
            api(`/api/dashboard/analytics/top-categories?${params({ type: "INCOME", startDate: start, endDate: end, limit: 5 })}`),
            api(`/api/dashboard/analytics/top-categories?${params({ type: "EXPENSE", startDate: start, endDate: end, limit: 5 })}`)
        ]);
        let trends = monthly || [];
        if (!trends.length) {
            trends = buildMonthlyTrends((await api("/api/records?size=200&sortBy=transactionDate&sortDirection=asc")).content || []);
        }

        root.innerHTML = `
            ${pageHead("Analyst room", "A focused view of category pressure, recurring patterns, and period movement.", `<button class="button primary" data-action="refresh-analytics">Refresh analysis</button>`)}
            <section class="filter-panel">
                <form id="analytics-form" class="filter-row">
                    ${dateField("startDate", "Start", start)}
                    ${dateField("endDate", "End", end)}
                    <button class="text-button" type="submit">Apply period</button>
                </form>
            </section>
            <section class="section">
                ${sectionHead("Twelve month trend", "")}
                ${renderTrendGrid(trends)}
            </section>
            <section class="asymmetric-grid">
                <div class="section">
                    ${sectionHead("Expense breakdown", "")}
                    ${renderBars(expenses || [])}
                </div>
                <div class="section">
                    ${sectionHead("Top categories", "")}
                    ${renderCategorySplits("Income", topIncome || [])}
                    ${renderCategorySplits("Expense", topExpense || [])}
                </div>
            </section>
            <section class="section">
                ${sectionHead("Daily summary", `<span class="chip">${number((weekly || []).length)} weekly points</span>`)}
                ${renderDailyTable(daily || [])}
            </section>
        `;

        document.getElementById("analytics-form").addEventListener("submit", async (event) => {
            event.preventDefault();
            const form = event.currentTarget;
            localStorage.setItem("financeDashboard.analyticsStart", form.startDate.value || defaultStart);
            localStorage.setItem("financeDashboard.analyticsEnd", form.endDate.value || todayIso);
            await renderAnalytics();
        });
        bindRootActions();
    }

    async function renderRecords(pageNumber) {
        state.recordPage = Number.isInteger(pageNumber) ? pageNumber : state.recordPage;
        const filters = readRecordFilters();
        const data = await api(`/api/records?${params({ ...filters, page: state.recordPage, size: 12, sortBy: "transactionDate", sortDirection: "desc" })}`);
        state.records = data.content || [];

        root.innerHTML = `
            ${pageHead("Records ledger", "Search, filter, create, cancel, and reconcile income and expense records.", hasAnyRole(["ADMIN"]) ? `<button class="button primary" data-action="new-record">New record</button>` : "")}
            <section class="filter-panel">
                <form id="record-filter-form" class="filter-grid">
                    ${textField("search", "Search", filters.search || "", "Rent, salary, reference")}
                    ${selectField("type", "Type", filters.type || "", [["", "All"], ["INCOME", "Income"], ["EXPENSE", "Expense"]])}
                    ${selectField("status", "Status", filters.status || "", [["", "All"], ["PENDING", "Pending"], ["CONFIRMED", "Confirmed"], ["CANCELLED", "Cancelled"]])}
                    ${selectField("categoryIds", "Category", filters.categoryIds || "", [["", "All categories"], ...state.categories.map((category) => [category.id, category.name])])}
                    ${dateField("startDate", "Start", filters.startDate || "")}
                    ${dateField("endDate", "End", filters.endDate || "")}
                    ${textField("minAmount", "Minimum", filters.minAmount || "", "0.00", "number")}
                    ${textField("maxAmount", "Maximum", filters.maxAmount || "", "0.00", "number")}
                    <div class="field">
                        <span>&nbsp;</span>
                        <button class="text-button" type="submit">Apply filters</button>
                    </div>
                </form>
            </section>
            <section class="table-shell">
                ${renderRecordTable(state.records, hasAnyRole(["ADMIN"]))}
            </section>
            ${pagination(data, "records")}
        `;

        document.getElementById("record-filter-form").addEventListener("submit", async (event) => {
            event.preventDefault();
            writeRecordFilters(new FormData(event.currentTarget));
            state.recordPage = 0;
            await renderRecords(0);
        });
        bindRootActions();
    }

    async function renderCategories() {
        await loadCategories(true);
        const income = state.categories.filter((category) => category.type === "INCOME" || category.type === "BOTH");
        const expense = state.categories.filter((category) => category.type === "EXPENSE" || category.type === "BOTH");

        root.innerHTML = `
            ${pageHead("Category system", "Manage the taxonomy that keeps every record readable.", hasAnyRole(["ADMIN"]) ? `<button class="button primary" data-action="new-category">New category</button>` : "")}
            <section class="asymmetric-grid">
                <div class="section">
                    ${sectionHead("Income", `<span class="chip">${income.length} categories</span>`)}
                    ${renderCategoryList(income)}
                </div>
                <div class="section">
                    ${sectionHead("Expense", `<span class="chip">${expense.length} categories</span>`)}
                    ${renderCategoryList(expense)}
                </div>
            </section>
            <section class="section">
                ${sectionHead("Complete catalog", "")}
                ${renderCategoryTable(state.categories)}
            </section>
        `;
        bindRootActions();
    }

    async function renderUsers(pageNumber) {
        state.userPage = Number.isInteger(pageNumber) ? pageNumber : state.userPage;
        const search = localStorage.getItem("financeDashboard.userSearch") || "";
        const url = search
            ? `/api/users/search?${params({ query: search, page: state.userPage, size: 12 })}`
            : `/api/users?${params({ page: state.userPage, size: 12, sortBy: "createdAt", sortDir: "desc" })}`;
        const data = await api(url);
        state.users = data.content || [];

        root.innerHTML = `
            ${pageHead("People and roles", "Operate access across viewer, analyst, and administrator seats.", `<button class="button primary" data-action="new-user">New user</button>`)}
            <section class="filter-panel">
                <form id="user-search-form" class="filter-row">
                    ${textField("query", "Search users", search, "Username, email, name")}
                    <button class="text-button" type="submit">Search</button>
                    <button class="text-button" type="button" data-action="clear-user-search">Clear</button>
                </form>
            </section>
            <section class="table-shell">
                ${renderUserTable(state.users)}
            </section>
            ${pagination(data, "users")}
        `;

        document.getElementById("user-search-form").addEventListener("submit", async (event) => {
            event.preventDefault();
            localStorage.setItem("financeDashboard.userSearch", event.currentTarget.query.value.trim());
            state.userPage = 0;
            await renderUsers(0);
        });
        bindRootActions();
    }

    async function renderAudit(pageNumber) {
        state.auditPage = Number.isInteger(pageNumber) ? pageNumber : state.auditPage;
        const hoursBack = localStorage.getItem("financeDashboard.auditHours") || "24";
        const [logs, stats] = await Promise.all([
            api(`/api/audit?${params({ page: state.auditPage, size: 16 })}`),
            api(`/api/audit/stats?${params({ hoursBack })}`)
        ]);

        root.innerHTML = `
            ${pageHead("Audit trail", "A compact record of identity, data changes, and access events.", `<button class="button primary" data-action="refresh-audit">Refresh audit</button>`)}
            <section class="filter-panel">
                <form id="audit-form" class="filter-row">
                    ${selectField("hoursBack", "Window", hoursBack, [["24", "24 hours"], ["72", "72 hours"], ["168", "7 days"], ["720", "30 days"]])}
                    <button class="text-button" type="submit">Apply window</button>
                </form>
            </section>
            <section class="section">
                ${sectionHead("Action volume", "")}
                <div class="mini-stats">
                    ${Object.entries(stats || {}).map(([key, value]) => `<span class="chip">${escapeHtml(key)} ${number(value)}</span>`).join("") || `<p class="muted">No audit volume in this window.</p>`}
                </div>
            </section>
            <section class="table-shell">
                ${renderAuditTable(logs.content || [])}
            </section>
            ${pagination(logs, "audit")}
        `;

        document.getElementById("audit-form").addEventListener("submit", async (event) => {
            event.preventDefault();
            localStorage.setItem("financeDashboard.auditHours", event.currentTarget.hoursBack.value);
            state.auditPage = 0;
            await renderAudit(0);
        });
        bindRootActions();
    }

    async function renderProfile() {
        const profile = await api("/api/users/profile");
        const activeTab = localStorage.getItem("financeDashboard.profileTab") || "profile";

        root.innerHTML = `
            ${pageHead("Profile", "Keep identity, contact details, and session security close at hand.", "")}
            <section class="section profile-card">
                <div class="tab-strip" role="tablist">
                    <button class="${activeTab === "profile" ? "is-active" : ""}" type="button" data-action="profile-tab" data-tab="profile">Profile</button>
                    <button class="${activeTab === "password" ? "is-active" : ""}" type="button" data-action="profile-tab" data-tab="password">Password</button>
                </div>
                ${activeTab === "password" ? renderPasswordForm() : renderProfileForm(profile)}
            </section>
        `;
        bindRootActions();
        if (activeTab === "password") {
            document.getElementById("password-form").addEventListener("submit", changePassword);
        } else {
            document.getElementById("profile-form").addEventListener("submit", saveProfile);
        }
    }

    function bindRootActions() {
        root.querySelectorAll("[data-action]").forEach((element) => {
            element.addEventListener("click", handleAction);
        });
    }

    async function handleAction(event) {
        const target = event.currentTarget;
        const action = target.dataset.action;
        const id = Number(target.dataset.id);

        try {
            if (action === "new-record") openRecordDialog();
            if (action === "edit-record") openRecordDialog(state.records.find((record) => record.id === id));
            if (action === "delete-record") await deleteRecord(id);
            if (action === "cancel-record") await cancelRecord(id);
            if (action === "records-prev") await renderRecords(state.recordPage - 1);
            if (action === "records-next") await renderRecords(state.recordPage + 1);
            if (action === "new-category") openCategoryDialog();
            if (action === "edit-category") openCategoryDialog(state.categories.find((category) => category.id === id));
            if (action === "delete-category") await deleteCategory(id);
            if (action === "new-user") openUserDialog();
            if (action === "edit-user") openUserDialog(state.users.find((user) => user.id === id));
            if (action === "activate-user") await setUserStatus(id, "activate");
            if (action === "deactivate-user") await setUserStatus(id, "deactivate");
            if (action === "delete-user") await deleteUser(id);
            if (action === "users-prev") await renderUsers(state.userPage - 1);
            if (action === "users-next") await renderUsers(state.userPage + 1);
            if (action === "clear-user-search") {
                localStorage.removeItem("financeDashboard.userSearch");
                state.userPage = 0;
                await renderUsers(0);
            }
            if (action === "refresh-audit") await renderAudit(state.auditPage);
            if (action === "audit-prev") await renderAudit(state.auditPage - 1);
            if (action === "audit-next") await renderAudit(state.auditPage + 1);
            if (action === "refresh-analytics") await renderAnalytics();
            if (action === "profile-tab") {
                localStorage.setItem("financeDashboard.profileTab", target.dataset.tab);
                await renderProfile();
            }
        } catch (error) {
            notify(error.message || "Action failed");
        }
    }

    function openRecordDialog(record) {
        const isEdit = Boolean(record);
        dialogKicker.textContent = isEdit ? "Update record" : "Create record";
        dialogTitle.textContent = isEdit ? "Record details" : "New record";
        const selectedType = record ? record.type : "EXPENSE";
        dialogBody.innerHTML = `
            <form id="record-form" class="stack">
                <div class="form-grid">
                    ${textField("amount", "Amount", record ? record.amount : "", "0.00", "number")}
                    ${selectField("type", "Type", selectedType, [["INCOME", "Income"], ["EXPENSE", "Expense"]])}
                    ${selectField("categoryId", "Category", record ? record.categoryId : "", categoryOptions(selectedType))}
                    ${dateField("transactionDate", "Transaction date", record ? record.transactionDate : todayIso)}
                    ${textField("description", "Description", record ? record.description : "", "Monthly rent")}
                    ${textField("referenceNumber", "Reference", record ? record.referenceNumber : "", "INV-1001")}
                    ${textField("tags", "Tags", record ? record.tags : "", "rent,monthly")}
                    ${textField("attachmentUrl", "Attachment URL", record ? record.attachmentUrl : "", "https://")}
                    <label class="checkbox-row span-2">
                        <input type="checkbox" name="isRecurring" ${record && record.isRecurring ? "checked" : ""}>
                        <span>Recurring record</span>
                    </label>
                    ${selectField("recurringFrequency", "Frequency", record && record.recurringFrequency ? record.recurringFrequency : "", [["", "None"], ["DAILY", "Daily"], ["WEEKLY", "Weekly"], ["BIWEEKLY", "Biweekly"], ["MONTHLY", "Monthly"], ["QUARTERLY", "Quarterly"], ["YEARLY", "Yearly"]], "span-2")}
                    ${textAreaField("notes", "Notes", record ? record.notes : "", "span-2")}
                </div>
                <div class="dialog-actions">
                    <button class="text-button" type="button" data-close-dialog>Cancel</button>
                    <button class="button primary" type="submit">${isEdit ? "Save record" : "Create record"}</button>
                </div>
            </form>
        `;
        openDialog();
        const form = document.getElementById("record-form");
        form.type.addEventListener("change", () => {
            form.categoryId.innerHTML = optionsHtml(categoryOptions(form.type.value), "");
        });
        form.querySelector("[data-close-dialog]").addEventListener("click", closeDialog);
        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            const payload = serializeRecordForm(form);
            if (isEdit) {
                await api(`/api/records/${record.id}`, { method: "PUT", body: JSON.stringify(payload) });
                notify("Record updated");
            } else {
                await api("/api/records", { method: "POST", body: JSON.stringify(payload) });
                notify("Record created");
            }
            closeDialog();
            if (page === "dashboard") await renderDashboard();
            if (page === "records") await renderRecords();
        });
    }

    function openCategoryDialog(category) {
        const isEdit = Boolean(category);
        dialogKicker.textContent = isEdit ? "Update category" : "Create category";
        dialogTitle.textContent = isEdit ? "Category details" : "New category";
        dialogBody.innerHTML = `
            <form id="category-form" class="stack">
                <div class="form-grid">
                    ${textField("name", "Name", category ? category.name : "", "Office supplies")}
                    ${selectField("type", "Type", category ? category.type : "EXPENSE", [["INCOME", "Income"], ["EXPENSE", "Expense"], ["BOTH", "Both"]])}
                    ${textField("color", "Color", category ? category.color : "#9A6B50", "#9A6B50")}
                    ${textField("icon", "Icon", category ? category.icon : "", "briefcase")}
                    ${selectField("parentId", "Parent", category && category.parentId ? category.parentId : "", [["", "No parent"], ...state.categories.filter((item) => !category || item.id !== category.id).map((item) => [item.id, item.name])], "span-2")}
                    ${textAreaField("description", "Description", category ? category.description : "", "span-2")}
                </div>
                <div class="dialog-actions">
                    <button class="text-button" type="button" data-close-dialog>Cancel</button>
                    <button class="button primary" type="submit">${isEdit ? "Save category" : "Create category"}</button>
                </div>
            </form>
        `;
        openDialog();
        const form = document.getElementById("category-form");
        form.querySelector("[data-close-dialog]").addEventListener("click", closeDialog);
        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            const data = Object.fromEntries(new FormData(form).entries());
            const payload = {
                name: data.name,
                description: emptyToNull(data.description),
                type: data.type,
                color: data.color || "#9A6B50",
                icon: emptyToNull(data.icon),
                parentId: data.parentId ? Number(data.parentId) : null
            };
            if (isEdit) {
                await api(`/api/categories/${category.id}`, { method: "PUT", body: JSON.stringify(payload) });
                notify("Category updated");
            } else {
                await api("/api/categories", { method: "POST", body: JSON.stringify(payload) });
                notify("Category created");
            }
            closeDialog();
            await renderCategories();
        });
    }

    function openUserDialog(user) {
        const isEdit = Boolean(user);
        dialogKicker.textContent = isEdit ? "Update user" : "Create user";
        dialogTitle.textContent = isEdit ? "User details" : "New user";
        dialogBody.innerHTML = `
            <form id="user-form" class="stack">
                <div class="form-grid">
                    ${!isEdit ? textField("username", "Username", "", "newuser") : ""}
                    ${textField("email", "Email", user ? user.email : "", "name@example.com", "email")}
                    ${!isEdit ? textField("password", "Password", "", "Test@1234", "password") : ""}
                    ${textField("firstName", "First name", user ? user.firstName : "", "First")}
                    ${textField("lastName", "Last name", user ? user.lastName : "", "Last")}
                    ${isEdit ? selectField("status", "Status", user.status || "ACTIVE", [["ACTIVE", "Active"], ["INACTIVE", "Inactive"], ["SUSPENDED", "Suspended"]]) : ""}
                    <fieldset class="span-2">
                        <legend>Roles</legend>
                        ${roleCheckboxes(user ? user.roles : ["VIEWER"])}
                    </fieldset>
                </div>
                <div class="dialog-actions">
                    <button class="text-button" type="button" data-close-dialog>Cancel</button>
                    <button class="button primary" type="submit">${isEdit ? "Save user" : "Create user"}</button>
                </div>
            </form>
        `;
        openDialog();
        const form = document.getElementById("user-form");
        form.querySelector("[data-close-dialog]").addEventListener("click", closeDialog);
        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            const data = Object.fromEntries(new FormData(form).entries());
            const roles = Array.from(form.querySelectorAll("input[name='roles']:checked")).map((input) => input.value);
            const payload = {
                email: data.email,
                firstName: emptyToNull(data.firstName),
                lastName: emptyToNull(data.lastName),
                roles
            };
            if (isEdit) {
                payload.status = data.status;
                await api(`/api/users/${user.id}`, { method: "PUT", body: JSON.stringify(payload) });
                notify("User updated");
            } else {
                payload.username = data.username;
                payload.password = data.password;
                await api("/api/users", { method: "POST", body: JSON.stringify(payload) });
                notify("User created");
            }
            closeDialog();
            await renderUsers();
        });
    }

    async function saveProfile(event) {
        event.preventDefault();
        const form = event.currentTarget;
        const data = Object.fromEntries(new FormData(form).entries());
        await api("/api/users/profile", {
            method: "PUT",
            body: JSON.stringify({
                email: data.email,
                firstName: emptyToNull(data.firstName),
                lastName: emptyToNull(data.lastName)
            })
        });
        notify("Profile saved");
        state.user = await api("/api/auth/me");
        hydrateShell();
    }

    async function changePassword(event) {
        event.preventDefault();
        const form = event.currentTarget;
        const data = Object.fromEntries(new FormData(form).entries());
        await api("/api/users/change-password", {
            method: "POST",
            body: JSON.stringify(data)
        });
        form.reset();
        notify("Password changed");
    }

    async function deleteRecord(id) {
        if (!window.confirm("Delete this record?")) return;
        await api(`/api/records/${id}`, { method: "DELETE" });
        notify("Record deleted");
        await renderRecords();
    }

    async function cancelRecord(id) {
        if (!window.confirm("Cancel this record?")) return;
        await api(`/api/records/${id}/cancel`, { method: "POST" });
        notify("Record cancelled");
        await renderRecords();
    }

    async function deleteCategory(id) {
        if (!window.confirm("Delete this category?")) return;
        await api(`/api/categories/${id}`, { method: "DELETE" });
        notify("Category deleted");
        await renderCategories();
    }

    async function setUserStatus(id, action) {
        await api(`/api/users/${id}/${action}`, { method: "POST" });
        notify(action === "activate" ? "User activated" : "User deactivated");
        await renderUsers();
    }

    async function deleteUser(id) {
        if (!window.confirm("Delete this user?")) return;
        await api(`/api/users/${id}`, { method: "DELETE" });
        notify("User deleted");
        await renderUsers();
    }

    async function logout() {
        try {
            await api("/api/auth/logout", { method: "POST" });
        } catch (error) {
            // A stale token should still clear the local session.
        }
        localStorage.removeItem(AUTH_KEY);
        window.location.assign("/login");
    }

    async function loadCategories(force) {
        if (state.categories.length && !force) {
            return;
        }
        state.categories = await api("/api/categories");
    }

    function renderRecordTable(records, showActions) {
        if (!records.length) {
            return empty("No records found", "Adjust filters or create a new entry.");
        }
        return `
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Description</th>
                        <th>Category</th>
                        <th>Type</th>
                        <th>Amount</th>
                        <th>Status</th>
                        ${showActions ? "<th>Actions</th>" : ""}
                    </tr>
                </thead>
                <tbody>
                    ${records.map((record) => `
                        <tr>
                            <td>${formatDate(record.transactionDate)}</td>
                            <td><strong>${escapeHtml(record.description || "Untitled")}</strong><br><span class="muted">${escapeHtml(record.referenceNumber || record.tags || "")}</span></td>
                            <td>${swatch(record.categoryColor)}${escapeHtml(record.categoryName || "Unassigned")}</td>
                            <td><span class="status-pill">${escapeHtml(record.type)}</span></td>
                            <td class="amount">${money(record.amount)}</td>
                            <td>${escapeHtml(record.status || "")}</td>
                            ${showActions ? `<td class="inline-actions">
                                <button class="text-button" type="button" data-action="edit-record" data-id="${record.id}">Edit</button>
                                <button class="text-button" type="button" data-action="cancel-record" data-id="${record.id}">Cancel</button>
                                <button class="text-button" type="button" data-action="delete-record" data-id="${record.id}">Delete</button>
                            </td>` : ""}
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        `;
    }

    function renderDailyTable(rows) {
        if (!rows.length) {
            return empty("No daily activity", "The selected period has no confirmed records.");
        }
        return `
            <table class="data-table">
                <thead><tr><th>Date</th><th>Income</th><th>Expense</th><th>Balance</th></tr></thead>
                <tbody>
                    ${rows.map((row) => `
                        <tr>
                            <td>${formatDate(row.date)}</td>
                            <td class="amount">${money(row.income)}</td>
                            <td class="amount">${money(row.expense)}</td>
                            <td class="amount">${money(row.balance)}</td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        `;
    }

    function renderCategoryList(categories) {
        if (!categories.length) {
            return empty("No categories", "Create a category for this side of the ledger.");
        }
        return `<div class="category-list">${categories.map((category) => `
            <div class="split-list-item">
                <div>
                    <strong>${swatch(category.color)}${escapeHtml(category.name)}</strong>
                    <p>${escapeHtml(category.description || "No description")}</p>
                </div>
                <span class="chip">${category.isSystem ? "System" : "Custom"}</span>
            </div>
        `).join("")}</div>`;
    }

    function renderCategoryTable(categories) {
        if (!categories.length) {
            return empty("No categories", "The category catalog is empty.");
        }
        return `
            <table class="data-table">
                <thead><tr><th>Name</th><th>Type</th><th>Parent</th><th>Status</th>${hasAnyRole(["ADMIN"]) ? "<th>Actions</th>" : ""}</tr></thead>
                <tbody>
                    ${categories.map((category) => `
                        <tr>
                            <td>${swatch(category.color)}<strong>${escapeHtml(category.name)}</strong><br><span class="muted">${escapeHtml(category.description || "")}</span></td>
                            <td>${escapeHtml(category.type)}</td>
                            <td>${escapeHtml(category.parentName || "None")}</td>
                            <td>${category.isActive ? "Active" : "Inactive"} / ${category.isSystem ? "System" : "Custom"}</td>
                            ${hasAnyRole(["ADMIN"]) ? `<td class="inline-actions">
                                <button class="text-button" type="button" data-action="edit-category" data-id="${category.id}">Edit</button>
                                <button class="text-button" type="button" data-action="delete-category" data-id="${category.id}">Delete</button>
                            </td>` : ""}
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        `;
    }

    function renderUserTable(users) {
        if (!users.length) {
            return empty("No users found", "Search again or create a new user.");
        }
        return `
            <table class="data-table">
                <thead><tr><th>User</th><th>Roles</th><th>Status</th><th>Last login</th><th>Actions</th></tr></thead>
                <tbody>
                    ${users.map((user) => `
                        <tr>
                            <td><strong>${escapeHtml(user.fullName || fullName(user) || user.username)}</strong><br><span class="muted">${escapeHtml(user.email)}</span></td>
                            <td>${(user.roles || []).map((role) => `<span class="chip">${escapeHtml(cleanRole(role))}</span>`).join(" ")}</td>
                            <td>${escapeHtml(user.status || "")}</td>
                            <td>${formatDateTime(user.lastLoginAt)}</td>
                            <td class="inline-actions">
                                <button class="text-button" type="button" data-action="edit-user" data-id="${user.id}">Edit</button>
                                <button class="text-button" type="button" data-action="${user.status === "ACTIVE" ? "deactivate-user" : "activate-user"}" data-id="${user.id}">${user.status === "ACTIVE" ? "Deactivate" : "Activate"}</button>
                                <button class="text-button" type="button" data-action="delete-user" data-id="${user.id}">Delete</button>
                            </td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        `;
    }

    function renderAuditTable(logs) {
        if (!logs.length) {
            return empty("No audit entries", "The selected audit page is empty.");
        }
        return `
            <table class="data-table">
                <thead><tr><th>Time</th><th>Action</th><th>Entity</th><th>User</th><th>Description</th></tr></thead>
                <tbody>
                    ${logs.map((log) => `
                        <tr>
                            <td>${formatDateTime(log.createdAt)}</td>
                            <td><span class="status-pill">${escapeHtml(log.action)}</span></td>
                            <td>${escapeHtml(log.entityType)} #${escapeHtml(log.entityId)}</td>
                            <td>${escapeHtml(log.username || log.userId || "System")}</td>
                            <td>${escapeHtml(log.description || "")}</td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        `;
    }

    function renderProfileForm(profile) {
        return `
            <form id="profile-form" class="stack" style="margin-top: 24px;">
                <div class="form-grid">
                    ${textField("firstName", "First name", profile.firstName || "", "First")}
                    ${textField("lastName", "Last name", profile.lastName || "", "Last")}
                    ${textField("email", "Email", profile.email || "", "name@example.com", "email", "span-2")}
                </div>
                <div class="mini-stats">
                    <span class="chip">${escapeHtml(profile.username)}</span>
                    ${(profile.roles || []).map((role) => `<span class="chip">${escapeHtml(cleanRole(role))}</span>`).join("")}
                    <span class="chip">${escapeHtml(profile.status || "ACTIVE")}</span>
                </div>
                <div class="dialog-actions">
                    <button class="button primary" type="submit">Save profile</button>
                </div>
            </form>
        `;
    }

    function renderPasswordForm() {
        return `
            <form id="password-form" class="stack" style="margin-top: 24px;">
                <div class="form-grid">
                    ${textField("currentPassword", "Current password", "", "", "password", "span-2")}
                    ${textField("newPassword", "New password", "", "", "password")}
                    ${textField("confirmPassword", "Confirm password", "", "", "password")}
                </div>
                <div class="dialog-actions">
                    <button class="button primary" type="submit">Change password</button>
                </div>
            </form>
        `;
    }

    function renderBars(items) {
        if (!items.length) {
            return empty("No category data", "The selected period has no category totals.");
        }
        const max = Math.max(...items.map((item) => toNumber(item.amount)), 1);
        return `<div class="bar-list">${items.map((item) => `
            <div class="bar-row">
                <strong>${swatch(item.categoryColor)}${escapeHtml(item.categoryName)}</strong>
                <div class="bar-track"><div class="bar-fill" style="width: ${Math.max(2, toNumber(item.amount) / max * 100)}%"></div></div>
                <span class="amount">${money(item.amount)}</span>
            </div>
        `).join("")}</div>`;
    }

    function renderTrendGrid(trends) {
        if (!trends.length) {
            return empty("No trend data", "The current selection has no monthly movement.");
        }
        const ordered = [...trends].sort((a, b) => {
            const left = `${a.year || ""}-${String(a.month || "").padStart(2, "0")}`;
            const right = `${b.year || ""}-${String(b.month || "").padStart(2, "0")}`;
            return left.localeCompare(right);
        }).slice(-12);
        const max = Math.max(...ordered.flatMap((item) => [toNumber(item.income), toNumber(item.expense)]), 1);
        return `<div class="trend-grid">${ordered.map((item) => `
            <div class="trend-column">
                <div class="trend-bars" title="${money(item.income)} income, ${money(item.expense)} expense">
                    <span style="height: ${Math.max(2, toNumber(item.income) / max * 100)}%"></span>
                    <span style="height: ${Math.max(2, toNumber(item.expense) / max * 100)}%"></span>
                </div>
                <span class="trend-label">${escapeHtml(item.monthName || item.weekKey || item.date || "")}</span>
            </div>
        `).join("")}</div>`;
    }

    function renderCategorySplits(label, items) {
        return `
            <div style="margin-top: 18px;">
                <p class="eyebrow">${escapeHtml(label)}</p>
                ${items.length ? items.map((item) => `
                    <div class="split-list-item">
                        <div><strong>${escapeHtml(item.categoryName)}</strong><p>${number(item.transactionCount || 0)} transactions</p></div>
                        <span class="amount">${money(item.amount)}</span>
                    </div>
                `).join("") : `<p class="muted">No ${escapeHtml(label.toLowerCase())} categories in this period.</p>`}
            </div>
        `;
    }

    function pageHead(title, subtitle, action) {
        return `
            <section class="page-head">
                <div class="page-title">
                    <p class="eyebrow">${escapeHtml(pageTitle(page))}</p>
                    <h2>${escapeHtml(title)}</h2>
                    <p>${escapeHtml(subtitle)}</p>
                </div>
                <div class="entity-actions">${action || ""}</div>
            </section>
        `;
    }

    function sectionHead(title, action) {
        return `
            <div class="section-head">
                <h3 class="section-title">${escapeHtml(title)}</h3>
                <div>${action || ""}</div>
            </div>
        `;
    }

    function metric(label, value, extraClass) {
        return `
            <article class="metric ${extraClass || ""}">
                <p class="metric-label">${escapeHtml(label)}</p>
                <p class="metric-value">${escapeHtml(value)}</p>
            </article>
        `;
    }

    function pagination(data, type) {
        if (!data || data.totalPages <= 1) {
            return "";
        }
        return `
            <nav class="pagination" aria-label="${type} pagination">
                <button class="text-button" type="button" data-action="${type}-prev" ${data.first ? "disabled" : ""}>Previous</button>
                <span class="chip">Page ${number((data.page || 0) + 1)} of ${number(data.totalPages)}</span>
                <button class="text-button" type="button" data-action="${type}-next" ${data.last ? "disabled" : ""}>Next</button>
            </nav>
        `;
    }

    function empty(title, detail) {
        return `<div class="empty-state"><p class="eyebrow">${escapeHtml(title)}</p><p>${escapeHtml(detail)}</p></div>`;
    }

    function renderAccess(title) {
        root.innerHTML = `
            ${pageHead("Access reserved", `${title} is available to a narrower role in this workspace.`, `<a class="button primary" href="/dashboard">Return to dashboard</a>`)}
            <section class="section access-panel">
                <p class="eyebrow">Current role</p>
                <h3 class="section-title">${escapeHtml(primaryRole())}</h3>
                <p class="muted">Your account can still use the visible navigation items and profile controls.</p>
            </section>
        `;
    }

    function renderError(error) {
        root.innerHTML = `
            ${pageHead("Something broke", "The screen could not finish loading.", `<button class="button primary" type="button" onclick="window.location.reload()">Try again</button>`)}
            <section class="section">
                <p class="muted">${escapeHtml(error.message || "Unexpected error")}</p>
            </section>
        `;
    }

    function dateField(name, label, value, className) {
        return `<label class="${className || ""}"><span>${escapeHtml(label)}</span><input type="date" name="${name}" value="${escapeAttr(value || "")}" max="${todayIso}"></label>`;
    }

    function textField(name, label, value, placeholder, type, className) {
        return `<label class="${className || ""}"><span>${escapeHtml(label)}</span><input type="${type || "text"}" name="${name}" value="${escapeAttr(value || "")}" placeholder="${escapeAttr(placeholder || "")}"></label>`;
    }

    function textAreaField(name, label, value, className) {
        return `<label class="${className || ""}"><span>${escapeHtml(label)}</span><textarea name="${name}">${escapeHtml(value || "")}</textarea></label>`;
    }

    function selectField(name, label, value, options, className) {
        return `<label class="${className || ""}"><span>${escapeHtml(label)}</span><select name="${name}">${optionsHtml(options, value)}</select></label>`;
    }

    function optionsHtml(options, value) {
        return options.map(([optionValue, label]) => `<option value="${escapeAttr(optionValue)}" ${String(optionValue) === String(value) ? "selected" : ""}>${escapeHtml(label)}</option>`).join("");
    }

    function roleCheckboxes(roles) {
        const cleaned = new Set((roles || []).map(cleanRole));
        return ["VIEWER", "ANALYST", "ADMIN"].map((role) => `
            <label class="checkbox-row">
                <input type="checkbox" name="roles" value="${role}" ${cleaned.has(role) ? "checked" : ""}>
                <span>${role}</span>
            </label>
        `).join("");
    }

    function categoryOptions(type) {
        const allowed = state.categories.filter((category) => category.type === type || category.type === "BOTH");
        return allowed.map((category) => [category.id, category.name]);
    }

    function serializeRecordForm(form) {
        const data = Object.fromEntries(new FormData(form).entries());
        const isRecurring = Boolean(form.querySelector("input[name='isRecurring']").checked);
        return {
            amount: Number(data.amount),
            type: data.type,
            categoryId: Number(data.categoryId),
            description: emptyToNull(data.description),
            referenceNumber: emptyToNull(data.referenceNumber),
            transactionDate: data.transactionDate,
            notes: emptyToNull(data.notes),
            tags: emptyToNull(data.tags),
            isRecurring,
            recurringFrequency: isRecurring ? emptyToNull(data.recurringFrequency) : null,
            attachmentUrl: emptyToNull(data.attachmentUrl)
        };
    }

    function readRecordFilters() {
        try {
            return JSON.parse(localStorage.getItem("financeDashboard.recordFilters")) || {};
        } catch (error) {
            return {};
        }
    }

    function writeRecordFilters(formData) {
        const values = Object.fromEntries(formData.entries());
        const clean = {};
        Object.entries(values).forEach(([key, value]) => {
            if (value !== "") {
                clean[key] = value;
            }
        });
        localStorage.setItem("financeDashboard.recordFilters", JSON.stringify(clean));
    }

    async function api(url, options, retried) {
        const response = await fetch(url, {
            headers: {
                "Content-Type": "application/json",
                "Authorization": `${state.auth.tokenType || "Bearer"} ${state.auth.accessToken}`
            },
            ...options
        });
        if (response.status === 401 && !retried && state.auth.refreshToken) {
            await refreshToken();
            return api(url, options, true);
        }
        const payload = await parseJson(response);
        if (!response.ok || (payload && payload.success === false)) {
            const error = new Error(readMessage(payload, response.statusText));
            error.status = response.status;
            throw error;
        }
        return payload && Object.prototype.hasOwnProperty.call(payload, "data") ? payload.data : payload;
    }

    async function refreshToken() {
        const response = await fetch("/api/auth/refresh", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ refreshToken: state.auth.refreshToken })
        });
        const payload = await parseJson(response);
        if (!response.ok || !payload || payload.success === false) {
            localStorage.removeItem(AUTH_KEY);
            redirectToLogin();
            throw new Error("Session expired");
        }
        state.auth = {
            ...state.auth,
            accessToken: payload.data.accessToken,
            refreshToken: payload.data.refreshToken,
            tokenType: payload.data.tokenType || "Bearer",
            user: payload.data.user
        };
        writeAuth(state.auth);
    }

    async function parseJson(response) {
        try {
            return await response.json();
        } catch (error) {
            return null;
        }
    }

    function readMessage(payload, fallback) {
        if (!payload) return fallback || "Request failed";
        if (payload.message) return payload.message;
        if (payload.error) return payload.error;
        if (Array.isArray(payload.fieldErrors) && payload.fieldErrors.length) {
            return payload.fieldErrors.map((item) => item.message || item.field).join(", ");
        }
        return fallback || "Request failed";
    }

    function readAuth() {
        try {
            return JSON.parse(localStorage.getItem(AUTH_KEY));
        } catch (error) {
            return null;
        }
    }

    function writeAuth(auth) {
        localStorage.setItem(AUTH_KEY, JSON.stringify(auth));
    }

    function redirectToLogin() {
        localStorage.removeItem(AUTH_KEY);
        window.location.assign("/login");
    }

    function openDialog() {
        if (!dialog.open) {
            dialog.showModal();
        }
    }

    function closeDialog() {
        if (dialog.open) {
            dialog.close();
        }
    }

    function notify(text) {
        toast.textContent = text;
        toast.classList.add("is-visible");
        window.clearTimeout(notify.timer);
        notify.timer = window.setTimeout(() => toast.classList.remove("is-visible"), 2600);
    }

    function buildMonthlyTrends(records) {
        const groups = new Map();
        records
            .filter((record) => record.status === "CONFIRMED" && record.transactionDate)
            .forEach((record) => {
                const key = record.transactionDate.slice(0, 7);
                if (!groups.has(key)) {
                    const [year, month] = key.split("-").map(Number);
                    groups.set(key, {
                        year,
                        month,
                        monthName: new Date(year, month - 1, 1).toLocaleString(undefined, { month: "short" }),
                        income: 0,
                        expense: 0,
                        net: 0
                    });
                }
                const item = groups.get(key);
                if (record.type === "INCOME") item.income += toNumber(record.amount);
                if (record.type === "EXPENSE") item.expense += toNumber(record.amount);
                item.net = item.income - item.expense;
            });
        return Array.from(groups.values());
    }

    function params(object) {
        const search = new URLSearchParams();
        Object.entries(object || {}).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== "") {
                search.set(key, value);
            }
        });
        return search.toString();
    }

    function hasAnyRole(roles) {
        const currentRoles = ((state.user && state.user.roles) || []).map(cleanRole);
        return roles.some((role) => currentRoles.includes(cleanRole(role)));
    }

    function primaryRole() {
        const roles = ((state.user && state.user.roles) || []).map(cleanRole);
        return roles.includes("ADMIN") ? "Admin" : roles.includes("ANALYST") ? "Analyst" : "Viewer";
    }

    function cleanRole(role) {
        return String(role || "").replace("ROLE_", "").toUpperCase();
    }

    function fullName(user) {
        return [user.firstName, user.lastName].filter(Boolean).join(" ").trim();
    }

    function pageTitle(value) {
        return {
            dashboard: "Dashboard",
            analytics: "Analytics",
            records: "Records",
            categories: "Categories",
            users: "Users",
            audit: "Audit",
            profile: "Profile"
        }[value] || "Dashboard";
    }

    function money(value) {
        return new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 }).format(toNumber(value));
    }

    function number(value) {
        return new Intl.NumberFormat(undefined, { maximumFractionDigits: 2 }).format(toNumber(value));
    }

    function toNumber(value) {
        const parsed = Number(value);
        return Number.isFinite(parsed) ? parsed : 0;
    }

    function formatDate(value) {
        if (!value) return "Not set";
        return new Date(`${value}T00:00:00`).toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" });
    }

    function formatDateTime(value) {
        if (!value) return "Never";
        return new Date(value).toLocaleString(undefined, { month: "short", day: "numeric", year: "numeric", hour: "2-digit", minute: "2-digit" });
    }

    function toDateInput(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `${year}-${month}-${day}`;
    }

    function swatch(color) {
        return `<span class="swatch" style="background: ${sanitizeColor(color)}"></span>`;
    }

    function sanitizeColor(color) {
        return /^#[0-9A-Fa-f]{6}$/.test(color || "") ? color : "#9A6B50";
    }

    function emptyToNull(value) {
        return value && String(value).trim() ? String(value).trim() : null;
    }

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function escapeAttr(value) {
        return escapeHtml(value);
    }
})();

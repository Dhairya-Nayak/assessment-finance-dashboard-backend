(function () {
    const AUTH_KEY = "financeDashboard.auth";
    const message = document.getElementById("auth-message");
    const page = document.body.dataset.page || "login";
    const credentials = {
        admin: { usernameOrEmail: "testadmin1", password: "Test@1234" },
        analyst: { usernameOrEmail: "analyst1", password: "Test@1234" },
        viewer: { usernameOrEmail: "viewer1", password: "Viewer@1234" }
    };

    const currentAuth = readAuth();
    if (currentAuth && currentAuth.accessToken) {
        window.location.replace("/dashboard");
        return;
    }

    syncHeading();
    bindCredentialButtons();
    bindLogin();
    bindRegister();

    function syncHeading() {
        const title = document.getElementById("auth-title");
        const kicker = document.getElementById("auth-kicker");
        if (page === "register") {
            title.textContent = "Create account";
            kicker.textContent = "Start with viewer access";
        }
    }

    function bindCredentialButtons() {
        document.querySelectorAll("[data-credential]").forEach((button) => {
            button.addEventListener("click", () => {
                const sample = credentials[button.dataset.credential];
                if (!sample) {
                    return;
                }
                document.getElementById("login-identity").value = sample.usernameOrEmail;
                document.getElementById("login-password").value = sample.password;
                setMessage("");
            });
        });
    }

    function bindLogin() {
        const form = document.getElementById("login-form");
        if (!form) {
            return;
        }
        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            const submitter = form.querySelector("button[type='submit']");
            setBusy(submitter, true, "Signing in");
            setMessage("");
            try {
                const payload = Object.fromEntries(new FormData(form).entries());
                const auth = await request("/api/auth/login", {
                    method: "POST",
                    body: JSON.stringify(payload)
                });
                writeAuth(auth);
                window.location.assign("/dashboard");
            } catch (error) {
                setMessage(error.message || "Sign in failed");
            } finally {
                setBusy(submitter, false, "Sign in");
            }
        });
    }

    function bindRegister() {
        const form = document.getElementById("register-form");
        if (!form) {
            return;
        }
        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            const submitter = form.querySelector("button[type='submit']");
            setBusy(submitter, true, "Creating");
            setMessage("");
            try {
                const payload = Object.fromEntries(new FormData(form).entries());
                const auth = await request("/api/auth/register", {
                    method: "POST",
                    body: JSON.stringify(payload)
                });
                writeAuth(auth);
                window.location.assign("/dashboard");
            } catch (error) {
                setMessage(error.message || "Registration failed");
            } finally {
                setBusy(submitter, false, "Create account");
            }
        });
    }

    async function request(url, options) {
        const response = await fetch(url, {
            headers: { "Content-Type": "application/json" },
            ...options
        });
        let payload = null;
        try {
            payload = await response.json();
        } catch (error) {
            payload = null;
        }
        if (!response.ok || (payload && payload.success === false)) {
            throw new Error(readMessage(payload, response.statusText));
        }
        return payload && Object.prototype.hasOwnProperty.call(payload, "data") ? payload.data : payload;
    }

    function readMessage(payload, fallback) {
        if (!payload) {
            return fallback || "Request failed";
        }
        if (payload.message) {
            return payload.message;
        }
        if (payload.error) {
            return payload.error;
        }
        if (Array.isArray(payload.fieldErrors) && payload.fieldErrors.length) {
            return payload.fieldErrors.map((item) => item.message || item.field).join(", ");
        }
        return fallback || "Request failed";
    }

    function writeAuth(auth) {
        localStorage.setItem(AUTH_KEY, JSON.stringify({
            accessToken: auth.accessToken,
            refreshToken: auth.refreshToken,
            tokenType: auth.tokenType || "Bearer",
            expiresIn: auth.expiresIn,
            user: auth.user
        }));
    }

    function readAuth() {
        try {
            return JSON.parse(localStorage.getItem(AUTH_KEY));
        } catch (error) {
            return null;
        }
    }

    function setMessage(text) {
        message.textContent = text;
    }

    function setBusy(button, busy, text) {
        if (!button) {
            return;
        }
        button.disabled = busy;
        button.textContent = text;
    }
})();

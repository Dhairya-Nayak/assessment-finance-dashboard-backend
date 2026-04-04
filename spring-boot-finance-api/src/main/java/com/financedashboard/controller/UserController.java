package com.financedashboard.controller;

import com.financedashboard.dto.request.ChangePasswordRequest;
import com.financedashboard.dto.request.UserCreateRequest;
import com.financedashboard.dto.request.UserUpdateRequest;
import com.financedashboard.dto.response.ApiResponse;
import com.financedashboard.dto.response.PagedResponse;
import com.financedashboard.dto.response.UserDTO;
import com.financedashboard.security.CurrentUser;
import com.financedashboard.security.UserPrincipal;
import com.financedashboard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        PagedResponse<UserDTO> users = userService.getAllUsers(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PagedResponse<UserDTO> users = userService.searchUsers(query, page, size);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(@PathVariable String username) {
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsersByRole(@PathVariable String roleName) {
        List<UserDTO> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUserProfile(@CurrentUser UserPrincipal currentUser) {
        UserDTO user = userService.getCurrentUserProfile(currentUser);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
            @Valid @RequestBody UserCreateRequest request,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Creating new user: {} by admin: {}", request.getUsername(), currentUser.getUsername());
        UserDTO user = userService.createUser(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Updating user: {} by admin: {}", id, currentUser.getUsername());
        UserDTO user = userService.updateUser(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @Valid @RequestBody UserUpdateRequest request,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Updating profile for user: {}", currentUser.getUsername());
        UserDTO user = userService.updateUser(currentUser.getId(), request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Password change requested by user: {}", currentUser.getUsername());
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateUser(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Activating user: {} by admin: {}", id, currentUser.getUsername());
        userService.activateUser(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("User activated successfully"));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Deactivating user: {} by admin: {}", id, currentUser.getUsername());
        userService.deactivateUser(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        
        log.info("Deleting user: {} by admin: {}", id, currentUser.getUsername());
        userService.deleteUser(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
}

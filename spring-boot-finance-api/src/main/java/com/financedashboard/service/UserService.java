package com.financedashboard.service;

import com.financedashboard.dto.request.ChangePasswordRequest;
import com.financedashboard.dto.request.UserCreateRequest;
import com.financedashboard.dto.request.UserUpdateRequest;
import com.financedashboard.dto.response.PagedResponse;
import com.financedashboard.dto.response.UserDTO;
import com.financedashboard.entity.Role;
import com.financedashboard.entity.User;
import com.financedashboard.exception.BadRequestException;
import com.financedashboard.exception.DuplicateResourceException;
import com.financedashboard.exception.ResourceNotFoundException;
import com.financedashboard.repository.RoleRepository;
import com.financedashboard.repository.UserRepository;
import com.financedashboard.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserDTO.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return UserDTO.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserDTO> getAllUsers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> usersPage = userRepository.findAllActiveUsers(pageable);
        List<UserDTO> userDTOs = usersPage.getContent().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.from(usersPage, userDTOs);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserDTO> searchUsers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.searchUsers(search, pageable);
        List<UserDTO> userDTOs = usersPage.getContent().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.from(usersPage, userDTOs);
    }

    @Transactional
    public UserDTO createUser(UserCreateRequest request, Long createdBy) {
        // Check for duplicates
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Get roles
        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            String formattedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName.toUpperCase();
            Role role = roleRepository.findByName(formattedRoleName)
                    .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));
            roles.add(role);
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .status(User.UserStatus.ACTIVE)
                .createdBy(createdBy)
                .roles(roles)
                .build();

        user = userRepository.save(user);
        log.info("User created: {} by user {}", user.getUsername(), createdBy);

        return UserDTO.fromEntity(user);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserUpdateRequest request, Long modifiedBy) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("User", "email", request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        // Update roles if provided
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                String formattedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName.toUpperCase();
                Role role = roleRepository.findByName(formattedRoleName)
                        .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        user.setLastModifiedBy(modifiedBy);
        user = userRepository.save(user);
        log.info("User updated: {} by user {}", user.getUsername(), modifiedBy);

        return UserDTO.fromEntity(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Verify new password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getUsername());
    }

    @Transactional
    public void deactivateUser(Long id, Long modifiedBy) {
        userRepository.updateUserStatus(id, User.UserStatus.INACTIVE, modifiedBy);
        log.info("User deactivated: {} by user {}", id, modifiedBy);
    }

    @Transactional
    public void activateUser(Long id, Long modifiedBy) {
        userRepository.updateUserStatus(id, User.UserStatus.ACTIVE, modifiedBy);
        log.info("User activated: {} by user {}", id, modifiedBy);
    }

    @Transactional
    public void deleteUser(Long id, Long modifiedBy) {
        // Soft delete
        userRepository.updateUserStatus(id, User.UserStatus.DELETED, modifiedBy);
        log.info("User deleted: {} by user {}", id, modifiedBy);
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUserProfile(UserPrincipal currentUser) {
        return getUserById(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(String roleName) {
        String formattedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName.toUpperCase();
        return userRepository.findAllByRoleName(formattedRoleName).stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }
}

package com.fu.cinema.desktop.facade;

import com.fu.cinema.dto.SessionUser;
import com.fu.cinema.enums.Role;
import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthFacadeTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthFacade authFacade;

    @Test
    void loginAdmin_whenAdmin_shouldReturnSessionUser() {
        SessionUser admin = new SessionUser(1L, null, "Admin", Role.ADMIN);
        when(authService.login("admin@fucinema.vn", "Admin@123")).thenReturn(admin);

        SessionUser result = authFacade.loginAdmin("admin@fucinema.vn", "Admin@123");

        assertThat(result).isNotNull();
        assertThat(result.isAdmin()).isTrue();
        assertThat(result.getDisplayName()).isEqualTo("Admin");
    }

    @Test
    void loginAdmin_whenCustomer_shouldThrowBusinessException() {
        SessionUser customer = new SessionUser(2L, 10L, "Nguyễn Văn An", Role.CUSTOMER);
        when(authService.login("an.nv@gmail.com", "Customer@123")).thenReturn(customer);

        assertThatThrownBy(() -> authFacade.loginAdmin("an.nv@gmail.com", "Customer@123"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("chỉ dành cho nhân viên");
    }
}

package com.fu.cinema.desktop.facade;

import com.fu.cinema.desktop.model.GenreRow;
import com.fu.cinema.desktop.session.DesktopSession;
import com.fu.cinema.dto.GenreForm;
import com.fu.cinema.entity.Genre;
import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.service.GenreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenreFacadeTest {

    @Mock
    private GenreService genreService;

    @Mock
    private DesktopSession session;

    @InjectMocks
    private GenreFacade genreFacade;

    @Test
    void findAll_shouldReturnMappedGenreRows() {
        Genre g1 = new Genre("Hành động", "Phim hành động gay cấn");
        g1.setGenreId(1L);
        Genre g2 = new Genre("Kinh dị", "Phim kinh dị rùng rợn");
        g2.setGenreId(2L);

        when(genreService.findAll()).thenReturn(List.of(g1, g2));

        List<GenreRow> rows = genreFacade.findAll();

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).name()).isEqualTo("Hành động");
        assertThat(rows.get(1).name()).isEqualTo("Kinh dị");
    }

    @Test
    void save_whenNewGenre_shouldCheckAdminAndCreate() {
        GenreForm form = new GenreForm();
        form.setGenreName("Tài liệu");
        form.setDescription("Phim tài liệu thực tế");

        genreFacade.save(form);

        verify(session).requireAdmin();
        verify(genreService).create(form);
    }

    @Test
    void save_whenExistingGenre_shouldCheckAdminAndUpdate() {
        GenreForm form = new GenreForm();
        form.setGenreId(1L);
        form.setGenreName("Hành động cập nhật");
        form.setDescription("Mô tả mới");

        genreFacade.save(form);

        verify(session).requireAdmin();
        verify(genreService).update(1L, form);
    }

    @Test
    void delete_shouldCheckAdminAndDelete() {
        genreFacade.delete(1L);

        verify(session).requireAdmin();
        verify(genreService).delete(1L);
    }

    @Test
    void delete_whenServiceThrowsBusinessException_shouldPropagate() {
        doThrow(new BusinessException("Không thể xoá: vẫn còn phim thuộc thể loại"))
                .when(genreService).delete(1L);

        assertThatThrownBy(() -> genreFacade.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vẫn còn phim thuộc thể loại");
    }
}

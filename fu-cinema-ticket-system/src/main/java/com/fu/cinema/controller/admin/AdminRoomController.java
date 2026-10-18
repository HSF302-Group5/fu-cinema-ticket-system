package com.fu.cinema.controller.admin;

import com.fu.cinema.dto.RoomForm;
import com.fu.cinema.enums.RoomStatus;
import com.fu.cinema.enums.RoomType;
import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/rooms")
@RequiredArgsConstructor
public class AdminRoomController {

    private final RoomService roomService;

    // 1. Danh sách phòng chiếu
    @GetMapping
    public String list(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        return "admin/rooms/list";
    }

    // 2. Màn hình thêm mới phòng chiếu
    @GetMapping("/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("roomForm")) {
            model.addAttribute("roomForm", new RoomForm());
        }
        addCommonAttributes(model, false);
        return "admin/rooms/form";
    }

    // 3. Xử lý lưu phòng chiếu mới (POST - Post/Redirect/Get)
    @PostMapping
    public String create(@Valid @ModelAttribute("roomForm") RoomForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model, false);
            return "admin/rooms/form";
        }

        try {
            roomService.create(form);
            redirectAttributes.addFlashAttribute("success",
                    "Thêm phòng chiếu '" + form.getRoomName() + "' thành công!");
            return "redirect:/admin/rooms";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            addCommonAttributes(model, false);
            return "admin/rooms/form";
        }
    }

    // 4. Màn hình chỉnh sửa phòng chiếu
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        if (!model.containsAttribute("roomForm")) {
            model.addAttribute("roomForm", roomService.getForm(id));
        }
        addCommonAttributes(model, true);
        return "admin/rooms/form";
    }

    // 5. Xử lý cập nhật phòng chiếu
    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("roomForm") RoomForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model, true);
            return "admin/rooms/form";
        }

        try {
            roomService.update(id, form);
            redirectAttributes.addFlashAttribute("success",
                    "Cập nhật phòng chiếu '" + form.getRoomName() + "' thành công!");
            return "redirect:/admin/rooms";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            addCommonAttributes(model, true);
            return "admin/rooms/form";
        }
    }

    // 6. Xử lý xoá phòng chiếu (Tuân thủ BR-04 / TODO 22)
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        boolean fullyDeleted = roomService.delete(id);
        if (fullyDeleted) {
            redirectAttributes.addFlashAttribute("success", "Đã xoá phòng chiếu thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "Phòng chiếu đã có suất chiếu trong hệ thống nên không thể xoá hẳn. Đã chuyển trạng thái sang BẢO TRÌ (MAINTENANCE).");
        }
        return "redirect:/admin/rooms";
    }

    private void addCommonAttributes(Model model, boolean isEdit) {
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("roomStatuses", RoomStatus.values());
    }
}

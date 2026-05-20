package com.example.studentmanager.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.studentmanager.entity.Student;
import com.example.studentmanager.service.StudentService;

@Controller
@RequestMapping("/students")
public class StudentController {

    private static final int PAGE_SIZE = 10;

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // 1. Hiển thị tất cả sinh viên + form thêm
    @GetMapping
    public String listStudents(@RequestParam(defaultValue = "0") int page, Model model) {
        addStudentsPage(model, page);
        model.addAttribute("student", new Student());
        return "students";
    }

    // 2. Lưu (thêm hoặc cập nhật)
    @PostMapping("/save")
    public String saveStudent(@ModelAttribute("student") Student student) {
        studentService.save(student);
        return "redirect:/students";
    }

    // 3. Sửa (load dữ liệu lên form)
    @GetMapping("/edit/{id}")
    public String editStudent(@PathVariable Integer id, Model model) {
        Student student = studentService.getById(id);

        model.addAttribute("student", student);
        addStudentsPage(model, 0);

        return "students";
    }

    // 4. Xóa
    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Integer id) {
        studentService.delete(id);
        return "redirect:/students";
    }

    // 5. 🔍 Tìm kiếm theo tên hoặc ID, trống thì hiển thị tất cả
    @GetMapping("/search")
    public String searchStudents(@RequestParam(value = "keyword", required = false) String keyword,Model model) {

        List<Student> result = new ArrayList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            result = studentService.getAllStudents();
        } else {
            try {
                // Nếu nhập số → tìm theo ID
                Integer id = Integer.parseInt(keyword);
                Student student = studentService.getById(id);
                if (student != null) {
                    result.add(student);
                }
            } catch (NumberFormatException e) {
                // Nếu nhập chữ → tìm theo tên
                result = studentService.findByName(keyword);
            }
        }

        model.addAttribute("students", result);
        model.addAttribute("student", new Student());
        model.addAttribute("keyword", keyword);

        return "students";
    }

    private void addStudentsPage(Model model, int page) {
        int currentPage = Math.max(page, 0);
        Page<Student> studentPage = studentService.getStudentsPage(PageRequest.of(currentPage, PAGE_SIZE));

        if (studentPage.getTotalPages() > 0 && currentPage >= studentPage.getTotalPages()) {
            currentPage = studentPage.getTotalPages() - 1;
            studentPage = studentService.getStudentsPage(PageRequest.of(currentPage, PAGE_SIZE));
        }

        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", studentPage.getTotalPages());
        model.addAttribute("totalItems", studentPage.getTotalElements());
    }
}

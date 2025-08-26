package vn.edu.iuh.fit.innovationmanagementsystem_be.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.edu.iuh.fit.innovationmanagementsystem_be.service.FormFieldService;

@RestController
@RequestMapping("/api/v1/form-fields")
public class FormFieldController {

    private final FormFieldService formFieldService;

    public FormFieldController(FormFieldService formFieldService) {
        this.formFieldService = formFieldService;
    }

}

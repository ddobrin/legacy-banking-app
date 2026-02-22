package com.banking.cif.ui.view;

import com.banking.cif.dto.CustomerDTO;
import com.banking.cif.dto.CustomerRequest;
import com.banking.cif.service.CustomerService;
import com.banking.cif.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

@Route(value = "customer-form", layout = MainLayout.class)
public class CustomerFormView extends VerticalLayout implements HasUrlParameter<Integer> {

    private final CustomerService customerService;
    private Integer customerId;

    private TextField firstName = new TextField("First Name");
    private TextField lastName = new TextField("Last Name");
    private TextField email = new TextField("Email");
    private DatePicker dateOfBirth = new DatePicker("Date of Birth");

    private Button save = new Button("Save");
    private Button cancel = new Button("Cancel");

    private Binder<CustomerRequest> binder = new Binder<>(CustomerRequest.class);

    public CustomerFormView(CustomerService customerService) {
        this.customerService = customerService;
        
        binder.bindInstanceFields(this);

        add(createFormLayout(), createButtonLayout());
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Integer parameter) {
        this.customerId = parameter;
        if (parameter != null) {
            CustomerDTO customer = customerService.getCustomerById(parameter);
            CustomerRequest request = new CustomerRequest();
            request.setFirstName(customer.getFirstName());
            request.setLastName(customer.getLastName());
            request.setEmail(customer.getEmail());
            request.setDateOfBirth(customer.getDateOfBirth());
            binder.readBean(request);
        } else {
            binder.readBean(new CustomerRequest());
        }
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(firstName, lastName, email, dateOfBirth);
        return formLayout;
    }

    private HorizontalLayout createButtonLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(event -> validateAndSave());
        cancel.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate(CustomerListView.class)));
        return new HorizontalLayout(save, cancel);
    }

    private void validateAndSave() {
        try {
            CustomerRequest request = new CustomerRequest();
            binder.writeBean(request);
            if (customerId == null) {
                customerService.createCustomer(request);
            } else {
                customerService.updateCustomer(customerId, request);
            }
            Notification.show("Customer saved successfully.");
            getUI().ifPresent(ui -> ui.navigate(CustomerListView.class));
        } catch (Exception e) {
            Notification.show("Error saving customer: " + e.getMessage());
        }
    }
}
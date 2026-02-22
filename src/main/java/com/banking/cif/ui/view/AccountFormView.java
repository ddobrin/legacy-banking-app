package com.banking.cif.ui.view;

import com.banking.cif.dto.AccountRequest;
import com.banking.cif.model.Product;
import com.banking.cif.repository.ProductRepository;
import com.banking.cif.service.AccountService;
import com.banking.cif.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;

@Route(value = "account-form", layout = MainLayout.class)
public class AccountFormView extends VerticalLayout {

    private final AccountService accountService;

    private IntegerField customerId = new IntegerField("Customer ID");
    private ComboBox<Product> productCode = new ComboBox<>("Product");
    private TextField balance = new TextField("Initial Balance");

    private Button save = new Button("Save");
    private Button cancel = new Button("Cancel");

    public AccountFormView(AccountService accountService, ProductRepository productRepository) {
        this.accountService = accountService;

        customerId.setRequiredIndicatorVisible(true);
        productCode.setRequiredIndicatorVisible(true);
        productCode.setItems(productRepository.findAll());
        productCode.setItemLabelGenerator(p -> p.getName() + " (" + p.getProductCode() + ")");
        balance.setPlaceholder("0.00");

        add(createFormLayout(), createButtonLayout());
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(customerId, productCode, balance);
        return formLayout;
    }

    private HorizontalLayout createButtonLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(event -> validateAndSave());
        cancel.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate(AccountListView.class)));
        return new HorizontalLayout(save, cancel);
    }

    private void validateAndSave() {
        try {
            if (customerId.getValue() == null) {
                Notification.show("Customer ID is required.");
                return;
            }
            if (productCode.getValue() == null) {
                Notification.show("Product is required.");
                return;
            }

            AccountRequest request = new AccountRequest();
            request.setCustomerId(customerId.getValue());
            request.setProductCode(productCode.getValue().getProductCode());

            String balanceText = balance.getValue();
            if (balanceText != null && !balanceText.isBlank()) {
                request.setBalance(new BigDecimal(balanceText));
            }

            accountService.createAccount(request);
            Notification.show("Account created successfully.");
            getUI().ifPresent(ui -> ui.navigate(AccountListView.class));
        } catch (NumberFormatException e) {
            Notification.show("Invalid balance format. Please enter a valid number.");
        } catch (Exception e) {
            Notification.show("Error creating account: " + e.getMessage());
        }
    }
}

package com.banking.cif.ui.view;

import com.banking.cif.dto.CustomerDTO;
import com.banking.cif.service.CustomerService;
import com.banking.cif.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@Route(value = "customers", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class CustomerListView extends VerticalLayout {

    private final CustomerService customerService;
    private final Grid<CustomerDTO> grid = new Grid<>(CustomerDTO.class, false);
    private final TextField filterText = new TextField();

    public CustomerListView(CustomerService customerService) {
        this.customerService = customerService;
        setSizeFull();
        configureGrid();
        
        add(getToolbar(), grid);
        updateList();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(CustomerDTO::getCustomerId).setHeader("ID");
        grid.addColumn(CustomerDTO::getCifNumber).setHeader("CIF");
        grid.addColumn(CustomerDTO::getFirstName).setHeader("First Name");
        grid.addColumn(CustomerDTO::getLastName).setHeader("Last Name");
        grid.addColumn(CustomerDTO::getEmail).setHeader("Email");
        grid.addColumn(CustomerDTO::getKycStatus).setHeader("KYC Status");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                getUI().ifPresent(ui -> ui.navigate(CustomerFormView.class, event.getValue().getCustomerId()));
            }
        });
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        Button addCustomerButton = new Button("Add Customer");
        addCustomerButton.addClickListener(click -> getUI().ifPresent(ui -> ui.navigate(CustomerFormView.class)));

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addCustomerButton);
        return toolbar;
    }

    private void updateList() {
        if (filterText.getValue() == null || filterText.getValue().isEmpty()) {
            grid.setItems(customerService.getAllCustomers());
        } else {
            grid.setItems(customerService.searchCustomersByName(filterText.getValue()));
        }
    }
}
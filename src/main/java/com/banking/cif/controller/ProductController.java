package com.banking.cif.controller;

import com.banking.cif.dto.ProductDto;
import com.banking.cif.service.ProductService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for querying banking product definitions. */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

  private final ProductService productService;

  /**
   * Creates a new {@link ProductController}.
   *
   * @param productService product service
   */
  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  /**
   * Lists all active banking products.
   *
   * @return list of active products
   */
  @GetMapping
  public List<ProductDto> getActiveProducts() {
    return productService.getActiveProducts();
  }
}

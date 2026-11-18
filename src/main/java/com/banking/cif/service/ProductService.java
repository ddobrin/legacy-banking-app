package com.banking.cif.service;

import com.banking.cif.dto.ProductDto;
import com.banking.cif.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service layer for querying available banking products. */
@Service
public class ProductService {

  private final ProductRepository productRepository;

  /**
   * Creates a new {@link ProductService}.
   *
   * @param productRepository product repository
   */
  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  /**
   * Lists all active banking products ordered by product code.
   *
   * @return list of active products
   */
  @Transactional(readOnly = true)
  public List<ProductDto> getActiveProducts() {
    return productRepository.findByActiveTrueOrderByProductCodeAsc().stream()
        .map(DtoMapper::toProductDto)
        .toList();
  }
}

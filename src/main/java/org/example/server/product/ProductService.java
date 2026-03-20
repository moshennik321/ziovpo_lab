package org.example.server.product;

import lombok.RequiredArgsConstructor;
import org.example.server.common.exception.ConflictException;
import org.example.server.common.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product getProductOrFail(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
    }

    public void checkNotBlocked(Product product) {
        if (product.isBlocked()) {
            throw new ConflictException("Product is blocked");
        }
    }
}
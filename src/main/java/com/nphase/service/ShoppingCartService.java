package com.nphase.service;

import com.nphase.entity.Product;
import com.nphase.entity.ShoppingCart;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShoppingCartService {
    static final double DISCOUNT_PERCENT = 10;
    static final int NUMBER_OF_PRODUCTS_TO_DISCOUNT = 3;


    Function<Product, BigDecimal> priceWithQuantity = product -> product.getPricePerUnit().multiply(BigDecimal.valueOf(product.getQuantity()));

    Function<Product, BigDecimal> applyDiscount = product -> priceWithQuantity.apply(product)
            .subtract((product.getPricePerUnit().multiply(BigDecimal.valueOf(product.getQuantity()))
                    .multiply(BigDecimal.valueOf(DISCOUNT_PERCENT))
                    .divide(BigDecimal.valueOf(100))));

    Function<Function<Product, BigDecimal>, Function<Product, BigDecimal>> priceWithQuantityDiscount =
            func -> product -> product.getQuantity() > NUMBER_OF_PRODUCTS_TO_DISCOUNT ?
                    applyDiscount.apply(product) :
                    priceWithQuantity.apply(product);

    Function<Product, BigDecimal> discountForQuantity = priceWithQuantityDiscount.apply(priceWithQuantity);

    public BigDecimal calculateTotalPrice(ShoppingCart shoppingCart) {
        return shoppingCart.getProducts()
                .stream()
                .map(priceWithQuantity)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal calculateTotalPriceWithDiscount(ShoppingCart shoppingCart) {
        return shoppingCart.getProducts()
                .stream()
                .map(discountForQuantity)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal calculateTotalPriceForCategoryDiscount(ShoppingCart shoppingCart) {
        Map<String, Integer> mapOfCategory = shoppingCart.getProducts()
                .stream()
                .collect(Collectors.groupingBy(
                        Product::getCategory,
                        Collectors.summingInt(Product::getQuantity)));

        return shoppingCart.getProducts()
                .stream()
                .map(product -> mapOfCategory.get(product.getCategory()) > NUMBER_OF_PRODUCTS_TO_DISCOUNT
                        ? applyDiscount.apply(product)
                        : priceWithQuantity.apply(product))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

    }
}

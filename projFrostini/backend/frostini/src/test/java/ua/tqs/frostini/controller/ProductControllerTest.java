package ua.tqs.frostini.controller;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import ua.tqs.frostini.datamodels.ProductDTO;
import ua.tqs.frostini.models.Product;
import ua.tqs.frostini.service.ProductService;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@WebMvcTest(value = ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {
    private String API_PRODUCTS_ENDPOINT = "api/v1/products";

    ProductDTO productDTO;
    Product product;
    Product unavailableProduct;
    List<Product> allproducts;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ProductService productService;

    @BeforeEach
    void setUp(){
        RestAssuredMockMvc.mockMvc( mvc );
        productDTO = createProductDTO();
        product = new Product(productDTO);
        unavailableProduct = new Product(productDTO);
        unavailableProduct.setStockQuantity(0);

        allproducts.add(product);
        allproducts.add(unavailableProduct);
    }
    //add product
    @Test
    void testWhenCreateValidProduct_thenReturnCreatedProduct(){
        when(productService.createProduct()).thenReturn(product);
        given()
            .contentType(ContentType.JSON).body(productDTO).post(API_PRODUCTS_ENDPOINT)
        .then().log().body().assertThat()
            .contentType(ContentType.JSON).and()
            .status(HttpStatus.OK).and()
            .body("price", is(product.getPrice())).and()
            .body("size()", is(1));

        verify(productService, times(1)).createProduct();
    }

    @Test
    void testWhenCreateInvalidProduct_thenReturnBadRequest(){
        when(productService.createProduct()).thenReturn(product);
        given()
            .contentType(ContentType.JSON).body("invalid product").post(API_PRODUCTS_ENDPOINT)
        .then().log().body().assertThat()
            .status(HttpStatus.BAD_REQUEST);

        verify(productService, times(1)).createProduct();
    }

    //remove product
    @Test
    void testWhenDeleteValidProduct_thenReturnNoContent(){
        when(productService.deleteProduct()).thenReturn(true);
        given()
            .delete(API_PRODUCTS_ENDPOINT+"/delete/"+product.getId())
        .then().log().body().assertThat()
            .status(HttpStatus.NO_CONTENT);

        verify(productService, times(1)).deleteProduct();
    }

    @Test
    void testWhenDeleteInvalidProduct_thenReturnBadRequest(){
        when(productService.deleteProduct()).thenReturn(false);
        given()
            .delete(API_PRODUCTS_ENDPOINT+"/delete/invalid_id")
        .then().log().body().assertThat()
            .status(HttpStatus.BAD_REQUEST);

        verify(productService, times(1)).deleteProduct();
    }

    //update product
    @Test
    void testWhenUpdateProductWithValidId_thenReturnUpdatedProduct(){
        product.setStockQuantity(8);
        productDTO.setStockQuantity(8);
        when(productService.editProduct()).thenReturn(product);
        given()
            .contentType(ContentType.JSON).body(productDTO).put(API_PRODUCTS_ENDPOINT+"/"+product.getId())
        .then().log().body().assertThat()
            .status(HttpStatus.OK).and()
            .contentType(ContentType.JSON).and()
            .body("size()",  is(1)).and()
            .body("stockQuantity", is(product.getStockQuantity()));

        verify(productService, times(1)).editProduct();
    }

    @Test
    void testWhenUpdateProductWithInvalidId_thenReturnBadRequest(){
        productDTO.setStockQuantity(8);
        when(productService.editProduct()).thenReturn(null);
        given()
            .contentType(ContentType.JSON).body(productDTO).put(API_PRODUCTS_ENDPOINT+"/invalid_id")
        .then().log().body().assertThat()
            .status(HttpStatus.BAD_REQUEST);

        verify(productService, times(1)).editProduct();
    }

    @Test
    void testWhenUpdateProductWithInvalidBody_thenReturnBadRequest(){
        when(productService.editProduct()).thenReturn(null);
        given()
            .contentType(ContentType.JSON).body("invalid body").put(API_PRODUCTS_ENDPOINT+"/"+product.getId())
        .then().log().body().assertThat()
            .status(HttpStatus.BAD_REQUEST);

        verify(productService, times(1)).editProduct();
    }

    //list all products
    @Test
    void testWhenGetAllProducts_thenReturnListOfProducts(){
        when(productService.getAllProducts()).thenReturn(allproducts);
        given()
            .get(API_PRODUCTS_ENDPOINT)
        .then().log().body().assertThat()
            .status(HttpStatus.OK).and()
            .contentType(ContentType.JSON).and()
            .body("size()", is(allproducts.size())).and()
            .body(is(allproducts));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void testWhenGetAllAvailableProducts_thenReturnListOfAvailableProducts(){
        allproducts.removeIf(s -> s.getStockQuantity()==0);
        List<Product> availableProducts = allproducts;

        when(productService.getAllAvailableProducts()).thenReturn(availableProducts);
        given()
            .get(API_PRODUCTS_ENDPOINT+"/available")
        .then().log().body().assertThat()
            .status(HttpStatus.OK).and()
            .contentType(ContentType.JSON).and()
            .body("size()", is(availableProducts.size())).and()
            .body(is(availableProducts));

        verify(productService, times(1)).getAllAvailableProducts();
    }

    @Test
    void testWhenGetAllUnavailableProducts_thenReturnListOfUnavailableProducts(){
        allproducts.removeIf(s -> s.getStockQuantity()>0);
        List<Product> unavailableProducts = allproducts;

        when(productService.getAllUnavailableProducts()).thenReturn(unavailableProducts);
        given()
            .get(API_PRODUCTS_ENDPOINT+"/available")
        .then().log().body().assertThat()
            .status(HttpStatus.OK).and()
            .contentType(ContentType.JSON).and()
            .body("size()", is(unavailableProducts.size())).and()
            .body(is(unavailableProducts));

        verify(productService, times(1)).getAllUnavailableProducts();
    }

    // list products according to a substring
    @Test
    void testWhenGetAllProductsWithFilteringBySubstring_thenReturnListOfProducts(){
        String substring = "kinder";
        Predicate<Product> bySubstring = product -> product.getName().containsIgnorecase(substring);
        List<Product> filteredProducts = allproducts.stream().filter(bySubstring).collect(Collectors.toList());

        when(productService.getProductsBySubstring()).thenReturn(filteredProducts);
        given()
            .get(API_PRODUCTS_ENDPOINT+"?substring="+substring)
        .then().log().body().assertThat()
            .status(HttpStatus.OK).and()
            .contentType(ContentType.JSON).and()
            .body("size()", is(filteredProducts.size())).and()
            .body(is(filteredProducts));

        verify(productService, times(1)).getProductsBySubstring();
    }

    //get product by id
    @Test
    void testWhenGetProductsByValidId_thenReturnProduct(){
        when(productService.getProductById()).thenReturn(product);
        given()
            .get(API_PRODUCTS_ENDPOINT+"/"+product.getId())
        .then().log().body().assertThat()
            .status(HttpStatus.OK).and()
            .contentType(ContentType.JSON).and()
            .body("size()", is(1)).and()
            .body(product);

        verify(productService, times(1)).getProductById();
    }

    @Test
    void testWhenGetProductsByInvalidId_thenReturnBadRequest(){
        when(productService.getProductById()).thenReturn(null);
        given()
            .get(API_PRODUCTS_ENDPOINT+"/"+product.getId())
        .then().log().body().assertThat()
            .status(HttpStatus.BAD_REQUEST);

        verify(productService, times(1)).getProductById();
    }

    private ProductDTO createProductDTO(){
        ProductDTO product = new ProductDTO();
        product.setPrice(9.99);
        product.setName("Kinder Bueno Ice Cream");
        product.setStockQuantity(10);
        return product;
    }
}
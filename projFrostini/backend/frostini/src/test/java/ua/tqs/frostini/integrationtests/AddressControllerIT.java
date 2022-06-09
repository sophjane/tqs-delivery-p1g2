package ua.tqs.frostini.integrationtests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import ua.tqs.frostini.datamodels.AddressDTO;
import ua.tqs.frostini.models.Address;
import ua.tqs.frostini.models.User;
import ua.tqs.frostini.repositories.AddressRepository;
import ua.tqs.frostini.repositories.UserRepository;

import static org.hamcrest.Matchers.equalTo;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AddressControllerIT {

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private String token;
    private User user;
    private Address address;
    private AddressDTO addressDTO;


    @Container
    static PostgreSQLContainer container = new PostgreSQLContainer( "postgres:11.12" )
        .withUsername( "demo" )
        .withPassword( "demopw" )
        .withDatabaseName( "shop" );


    @DynamicPropertySource
    static void properties( DynamicPropertyRegistry registry ) {
        registry.add( "spring.datasource.url", container::getJdbcUrl );
        registry.add( "spring.datasource.password", container::getPassword );
        registry.add( "spring.datasource.username", container::getUsername );
    }

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName( "Joaquim" );
        user.setPassword( "safepassword" );
        user.setEmail( "joaquim@ua.pt" );

        userRepository.saveAndFlush( user );

        address = new Address();
        address.setUser(user);
        address.setStreet("Street");
        address.setCity("City");
        address.setZipCode("234");

        addressRepository.saveAndFlush( address );
        addressDTO = new AddressDTO( user.getId(), "Street", "City", "234" );
    }

    @AfterEach
    void resetDb() {
        addressRepository.deleteAll();
        addressRepository.flush();
    }

    @Test
    public void testOnAddAddressAlreadyAdded_ThenReturn200() {
        RestAssured.given()
               .contentType( "application/json" )
               .body( addressDTO )
               .when()
               .post( createURL() )
               .then()
               .statusCode( 200 );
    }

    @Test
    public void testOnAddNewAddress_ThenReturn200() {
        AddressDTO aDto = new AddressDTO( user.getId(), "Street1", "City1", "2345" ); 
        RestAssured.given()
               .contentType( "application/json" )
               .body( aDto )
               .when()
               .post( createURL() )
               .then()
               .statusCode( 200 );
    }

    @Test
    public void testOnAddAddressWithInvalidUser_ThenReturnBadRequest() {
        AddressDTO aDto = new AddressDTO( 2l, "Street", "City", "234" ); 
        RestAssured.given()
               .contentType( "application/json" )
               .body( aDto )
               .when()
               .post( createURL() )
               .then()
               .statusCode( 400 );
    }

    String createURL() {
        return "http://localhost:" + randomServerPort + "/api/v1/addresses";
    }
}

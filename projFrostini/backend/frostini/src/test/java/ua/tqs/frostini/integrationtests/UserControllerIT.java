package ua.tqs.frostini.integrationtests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.frostini.datamodels.UserDTO;
import ua.tqs.frostini.models.User;
import ua.tqs.frostini.repositories.UserRepository;

import static org.hamcrest.Matchers.equalTo;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIT {
  @LocalServerPort
  int randomServerPort;

  @Autowired
  private UserRepository userRepository;


  private User user;
  private UserDTO userDTO;


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

    user = userRepository.saveAndFlush( user );
    userDTO = new UserDTO( "Joaquim", "safepassword", "joaquim@ua.pt" );
  }

  @AfterEach
  void resetDb() {
    userRepository.deleteAll();
    userRepository.flush();
  }

  @Test
  void testOnRegisterIfEmailIsUsed_ThenReturnConflict() {
    RestAssured.given()
               .contentType( "application/json" )
               .body( userDTO )
               .when()
               .post( createURL() )// post createURL() means register
               .then()
               .statusCode( 409 );

  }

  @Test
  void testRegisterUser_whenValidUserDTO_then200() {
    UserDTO newUser = new UserDTO( "Alfredo", "alfredoisnotbigenough", "alfredo@ua.pt" );

    RestAssured.given()
               .contentType( "application/json" )
               .body( newUser )
               .when()
               .post( createURL() )
               .then().log().body()
               .body( "name", equalTo( newUser.getName() ) )
               .body( "email", equalTo( newUser.getEmail() ) )
               .statusCode( 201 );
  }

  @Test
  void testRegisterUser_ThenInvalidUserDTO_thenBAD_REQUEST() {
    UserDTO newUser = new UserDTO( "alfredoJoaquim", "somepass", "not an email" );

    RestAssured.given()
               .contentType( "application/json" )
               .body( newUser )
               .when()
               .post( createURL() )
               .then()
               .statusCode( 400 );
  }

  @Test
  void testRegisterUser_whenInvalidUserDTO_thenBAD_REQUEST() {
    UserDTO newUser = new UserDTO( "alfredoJoaquim", "somepass", "not an email" );

    RestAssured.given()
               .contentType( "application/json" )
               .body( newUser )
               .when()
               .post( createURL() )
               .then()
               .statusCode( 400 );
  }


  @Test
  void testLoginUser_WithValidEmailAndUserAlredyRegistered_ThenReturnUser() {
    RestAssured.given()
               .contentType( "application/json" )
               .when()
               .get( createURL() + "/{email}" , userDTO.getEmail() )
               .then()
               .statusCode( 200 )
               .body( "name", equalTo( userDTO.getName() ) )
               .body( "email", equalTo( userDTO.getEmail() ) );
  }

  @Test
  void testLoginUser_WithValidEmailAndUserNotYetRegistered_ThenReturnResourceNotFound() {
    RestAssured.given()
               .contentType( "application/json" )
               .when()
               .get( createURL() + "/{email}","iamnotauseryet@ua.pt" )
               .then().log().body()
               .statusCode( 400 );
  }

  @Test
  void testLoginUser_WithInValidEmail_ThenReturnResourceNotFound() {
    RestAssured.given()
               .contentType( "application/json" )
               .when()
               .get( createURL() + "/{email}", "notarealemail" )
               .then()
               .statusCode( 400 );
  }


  String createURL() {
    return "http://localhost:" + randomServerPort + "/api/v1/user";
  }
}

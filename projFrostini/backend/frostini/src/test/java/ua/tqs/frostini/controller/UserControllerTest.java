package ua.tqs.frostini.controller;

import io.restassured.http.ContentType;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.frostini.datamodels.UserDTO;
import ua.tqs.frostini.exceptions.DuplicatedUserException;
import ua.tqs.frostini.models.User;
import ua.tqs.frostini.service.UserService;

import java.io.IOException;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;


@WebMvcTest(value = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private UserService userService;


  @BeforeEach
  void setUp() throws IOException {
    RestAssuredMockMvc.mockMvc( mvc );
  }

  @Test
  void testEndpointForCorrectPathWithValidRequestBodyReturn_ThenReturnCREATED() throws DuplicatedUserException {
    User u = createAndSaveUser( 1l );
    UserDTO uDto = createAndSaveUserDTO( 1l );
    when( userService.register( any() ) ).thenReturn( u );
    RestAssuredMockMvc.given().contentType( ContentType.JSON ).body( uDto )
                      .when().post( "api/v1/user" ).then()
                      .contentType( ContentType.JSON )
                      .status( HttpStatus.CREATED );

    verify( userService, times( 1 ) ).register( any() );
  }

  @Test
  void createNewUser_ShouldReturnTheUserThatHasBeenCreated() throws DuplicatedUserException {
    User u = createAndSaveUser( 1l );
    UserDTO uDto = createAndSaveUserDTO( 1l );
    when( userService.register( any() ) ).thenReturn( u );

    RestAssuredMockMvc.given().contentType( ContentType.JSON ).body( uDto ).when().post( "api/v1/user" )
                      .then()
                      .contentType( ContentType.JSON )
                      .status( HttpStatus.CREATED ).log()
                      .body().body( "name", equalTo( u.getName() ) )
                      .body( "email", equalTo( u.getEmail() )
                      );

    verify( userService, times( 1 ) ).register( any() );
  }


  @Test
  void createConflictingUser_ShouldReturnHttpStatusConflictAndItShouldNotUpdateThePreviousUser()
    throws DuplicatedUserException {
    User u = createAndSaveUser( 1l );
    UserDTO userDto = createAndSaveUserDTO( 1l );
    doThrow( new DuplicatedUserException( "User already exists!" ) ).when( userService ).register( userDto );

    RestAssuredMockMvc.given().contentType( ContentType.JSON ).body( userDto ).when().post( "api/v1/user" )
                      .then()
                      .status( HttpStatus.CONFLICT );

    verify( userService, times( 1 ) ).register( userDto );
  }

  @ParameterizedTest
  @MethodSource("invalidAccounts")
  void whenInvalidRegisterRequestBody_thenReturnStatus400( String name, String pwd, String email )
    throws DuplicatedUserException {
    UserDTO u = new UserDTO();
    u.setEmail( email );
    u.setName( name );
    u.setPwd( pwd );

    RestAssuredMockMvc.given()
                      .contentType( "application/json" )
                      .body( u )
                      .when()
                      .post( "api/v1/user" )
                      .then()
                      .statusCode( 400 );

    verify( userService, times( 0 ) ).register( any() );


  }


  private static Stream<Arguments> invalidAccounts() {
    return Stream.of(
      arguments( "Fernando", "12345", "fernando@ua.pt" ),
      arguments( "Fernando", "12345678", "fernandoua.pt" ),
      arguments( "", "12345", "fernando@ua.pt" ),
      arguments( null, "12345", "fernando@ua.pt" ),
      arguments( null, null, "fernando@ua.pt" ),
      arguments( null, "12345", null )
    );

  }


  @Test
  void testLoginEndpointForCorrectPathWithValidRequestBody_ThenReturnOkStatus()
    throws DuplicatedUserException {
    User u = createAndSaveUser( 1l );
    UserDTO uDto = createAndSaveUserDTO( 1l );

    when( userService.login( any() ) ).thenReturn( u );

    RestAssuredMockMvc.given()
                      .when().get( "api/v1/user/{email}", uDto.getEmail()  ).then()
                      .contentType( ContentType.JSON )
                      .status( HttpStatus.OK );

    verify( userService, times( 1 ) ).login( any() );
  }


  @Test
  void testLoginEndpointForCorrectPathWithValidPathParameterForEmail_ThenReturnCorrectUser() {
    User u = createAndSaveUser( 1l );
    UserDTO uDto = createAndSaveUserDTO( 1l );

    when( userService.login( any() ) ).thenReturn( u );

    RestAssuredMockMvc.given()
                      .when().get( "api/v1/user/{email}", uDto.getEmail() ).then()
                      .contentType( ContentType.JSON )
                      .status( HttpStatus.OK )
                      .body( "name", equalTo( u.getName() ) )
                      .body( "email", equalTo( u.getEmail() ) );

    verify( userService, times( 1 ) ).login( any() );
  }

  @Test
  void testLoginEndpointForCorrectPathWithUnusedEmail_ThenReturnStatusCodeResourceNotFound() {
    User u = createAndSaveUser( 1l );
    UserDTO uDto = createAndSaveUserDTO( 1l );
    when( userService.login( any() ) ).thenReturn( null );

    RestAssuredMockMvc.given()
                      .when().get( "api/v1/user/{email}", "unused@email.com" ) .then()
                      .status( HttpStatus.BAD_REQUEST );

    verify( userService, times( 1 ) ).login( any() );
  }


  @Test
  void testLoginEndpointForCorrectPathWithInvalidEmail_ThenReturnStatusBAD_Request() {
    when( userService.login( any() ) ).thenReturn( null );
    RestAssuredMockMvc.given()
                      .when().get( "api/v1/user/{email}", "notarealemail" ).then().log().body()
                      .status( HttpStatus.BAD_REQUEST );

    verify( userService, times( 0 ) ).login( any() );
  }

  /* -- helper -- */
  private User createAndSaveUser( long i ) {
    return new User( i, "Pedro", "safepassword", "pdfl" + i + "@ua.pt", false, null, null );
  }

  private UserDTO createAndSaveUserDTO( long l ) {
    return new UserDTO( "Pedro", "safepassword", "pdfl" + l + "@ua.pt" );
  }
}

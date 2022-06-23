package ua.tqs.delivera.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.tqs.delivera.datamodels.OrderDTO;
import ua.tqs.delivera.exceptions.NoRidersAvailable;
import ua.tqs.delivera.models.*;
import ua.tqs.delivera.repositories.LocationRepository;
import ua.tqs.delivera.repositories.OrderProfitRepository;
import ua.tqs.delivera.repositories.OrderRepository;
import ua.tqs.delivera.repositories.StoreRepository;
import ua.tqs.delivera.utils.DistanceCalculator;

import java.util.ArrayList;
import java.util.Optional;


@Service
public class OrderService {
  
  @Autowired
  OrderRepository orderRepository; // create and manage Orders
  
  @Autowired
  RiderService riderService;  // manageRiders
  
  @Autowired
  StoreRepository storeRepository; // create and look for stores
  
  @Autowired
  LocationRepository locationRepository; // create update find
  
  @Autowired
  OrderProfitRepository orderProfitRepository;
  
  ArrayList<Order> notAttributedOrders = new ArrayList<>();
  
  
  @Transactional
  public Order assignOrder( OrderDTO orderDTO ) {
    // get store
    String storeName = orderDTO.getStoreName();
    
    // create Store Location of the order
    Location storeLocation = new Location( orderDTO.getStoreLat(), orderDTO.getStoreLon() );
    
    // create Client Location of the order
    Location clientLocation = new Location( orderDTO.getClientLat(), orderDTO.getClientLon() );
    
    Optional<Store> storeOPT = storeRepository.findByName( storeName );
    Store store = null;
    
    if ( storeOPT.isEmpty() ) {
      Store storeToSave = new Store();
      
      storeToSave.setName( storeName );
      storeToSave.setAddress( storeLocation );
      
      store = storeRepository.save( storeToSave );
      locationRepository.save( storeLocation );
    }
  
    // Store exists
    if ( store == null ) {
      store = storeOPT.get();
    }
    // confirm store Location
    if ( ! storeLocation.equals( store.getAddress() ) ) {
      // update store Location
      store.setAddress( storeLocation );
    }
    
    // calculate distance between the 2 locations
    double distance = DistanceCalculator.distanceBetweenPointsOnEarth( clientLocation, storeLocation );
    
    // create the OrderProfit
    OrderProfit orderProfit = new OrderProfit();
    
    // distance * avg_motorcycle fuel consumption spent in 100 km / 100 km *
    // price of the gas * a percentage of the price of the order so that it is lucrative for the rider
    orderProfit.setOrderPrice( distance * 5 / 100 * 2 * .15 * orderDTO.getOrderPrice() );
    
    Order order = new Order();
    order.setOrderProfit( orderProfit );
    
    Rider riderToAssign = null;
    // get free riders and determine who is closer of the store
    try {
      riderToAssign = riderService.findClosestRider( storeLocation );
    } catch (NoRidersAvailable e) {
      //queue Order
      notAttributedOrders.add( order );
      return null;
    }
    order.setStore( store );
    order.setExternalId( orderDTO.getOrderStoreId() );
    
    orderProfit.setOrder( order );
    orderProfit.setRider( riderToAssign );
  
    riderToAssign = riderService.makeRiderUnavailable( riderToAssign );
    riderService.addProfitToRider( riderToAssign, orderProfit );
    orderProfit = orderProfitRepository.save( orderProfit );
    
    order.setOrderProfit( orderProfit );
    // set all fields and save new order
    // change Rider availability
    return orderRepository.save( order );
  }
  
}

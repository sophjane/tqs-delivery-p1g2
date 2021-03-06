import {
  Button,
  Card,
  CardActions,
  CardContent,
  Typography,
} from "@mui/material";
import CalendarTodayIcon from "@mui/icons-material/CalendarToday";
import FmdGoodIcon from "@mui/icons-material/FmdGood";
import { useNavigate } from "react-router-dom";
import { useContext, useEffect, useState } from 'react';
import "./OrdersHistoryPage.css";
import CheckIcon from "@mui/icons-material/Check";
import useLocalStorage from "../hooks/use-local-storage";
import AuthContext from '../context/AuthProvider';
import isAuthenticated from '../utils/Authentication';
import axios from '../api/axios';


const json = [
  {
    id: 1,
    date: "27-May-2022",
    address: "Universidade de Aveiro, 3810-193 Aveiro",
    state: "delivered",
    delivered: false,
  },
  {
    id: 2,
    date: "28-May-2022",
    address: "Universidade de Aveiro, 3810-193 Aveiro",
    state: "ordered",
    delivered: true,
  },
];

function OrdersHistoryPage() {
  const navigate = useNavigate();
  const { auth } = useContext(AuthContext);
  const isAuth = isAuthenticated(auth);
  const [state, setState] = useLocalStorage('state', '');
  const [orderInfo, setOrderInfo] = useState({});
  const [error, setError] = useState('');
  const [json, setJson] = useState([]);

  const viewOrder = (order) => {
    //console.log(order.id)
    setState(order.orderState);
    navigate(`/purchase/${order.id}`)
    //navigate('/purchase');
  };

  const sortOrders = (a, b) => {
    return b.id - a.id;
  }

  const printDate = (timestamp) => {
    return new Date(timestamp).toLocaleString();
  }
  

  useEffect(() => {
    if(!isAuth) {
      navigate('/login');
    }
  }, [isAuth]);

  useEffect(() => {
    const userId = auth.id;
    axios.get(`/order/user/${userId}`)
      .then(res => {
        const orders = res.data;
        orders.sort(sortOrders);
        console.log(orders);
        setJson(orders);
      }).catch(err => {
        setError('User does not have orders');
      })
  }, [json]);

  return (
    <div className="orders-history-page">
      <h1>Orders</h1>
      <div className="orders">
        { error !== '' ? 
          <p>{error}</p>
          : json.map((order, key) => (
          <div className="order">
            <Card sx={{ minWidth: 275 }} className="order-card">
              <CardContent>
                <Typography sx={{ mb: 1.5 }} color="#f8c8b9" fontWeight="900">
                  Order #{order.id}
                </Typography>
                <div className="order-info">
                  <div className="order-date">
                    <CalendarTodayIcon />
                    <Typography variant="body2" color="text.secondary">
                      {printDate(order.orderMadeTimeStamp)}
                    </Typography>
                  </div>
                  <div className="order-address">
                    <FmdGoodIcon />
                    <Typography variant="body2" color="text.secondary">
                      {`${order.address.latitude}, ${order.address.longitude}`}
                    </Typography>
                  </div>
                </div>
              </CardContent>
              <CardActions>
                <div className="order-details">
                  <Button
                    style={{
                      backgroundColor: "#f8c8b9",
                      color: "#ffffff",
                      fontWeight: "600",
                      fontSize: "12px",
                      border: "0",
                      height: "40px",
                    }}
                    variant="outlined"
                    size="small"
                    className="order-card-btn"
                    onClick={() => {
                      viewOrder(order)
                    }}
                  >
                    Check More About The Order
                  </Button>
                  {order.orderState === "delivered" && (
                    <div className="deliver-check">
                      <CheckIcon sx={{ color: "#9cb737" }} />
                      <p>Delivered</p>
                    </div>
                  )}
                </div>
              </CardActions>
            </Card>
          </div>
        ))
        }
      </div>
    </div>
  );
}
export default OrdersHistoryPage;

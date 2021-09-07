
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import ReservationManager from "./components/ReservationManager"

import CarManager from "./components/CarManager"

import CarList from "./components/CarList"
import PaymentManager from "./components/PaymentManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/reservations',
                name: 'ReservationManager',
                component: ReservationManager
            },

            {
                path: '/cars',
                name: 'CarManager',
                component: CarManager
            },

            {
                path: '/carLists',
                name: 'CarList',
                component: CarList
            },
            {
                path: '/payments',
                name: 'PaymentManager',
                component: PaymentManager
            },



    ]
})

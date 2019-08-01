/********************************************************
*                                                       *
*   Copyright (C) Microsoft. All rights reserved.       *
*                                                       *
********************************************************/

module Cauldron2 {

    type Pos : Integer32 where value > 0;

    type Computer : {
        cost : Pos;
    };

    type Application : {
        licenseFee : Pos;
    };

    type Server : {
    	cost : Pos;
        comp : Computer;
        app : Application;
    } where (value.cost == value.comp.cost + value.app.licenseFee) && value.cost < 2000;
    
    type DataCenter : {
    	serv : {Server*} where value.Count > 1 && value.Count < 13;
    } where (from serv in value.serv let s = 0 : Integer32 accumulate s + serv.cost) < 10001;   // sum of all costs

}

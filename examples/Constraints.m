/********************************************************
*                                                       *
*   Copyright (C) Microsoft. All rights reserved.       *
*                                                       *
********************************************************/

module Constraints
{
    type Person : { Name:Text; Age:Integer32; };
    type EligiblePerson : Person where value.Age > 17;
    type Marriage : { SpouseA: EligiblePerson; SpouseB: EligiblePerson; };
    
    PatChris(): Marriage
    {
      {SpouseA => {Name => "Pat", Age => 24},
       SpouseB => {Name => "Chris", Age => 32}}
    }
    
    /*
   //This does not work
    BillySam(): Marriage
    {
      {SpouseA => {Name => "Billy", Age => 4},
       SpouseB => {Name => "Sam", Age => 5}}
    }
    */
}

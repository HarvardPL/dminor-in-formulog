/********************************************************
*                                                       *
*   Copyright (C) Microsoft. All rights reserved.       *
*                                                       *
********************************************************/

module Fact
{

    factorial(n:Integer32) : Integer32
    {
        (n==0)? 1 : n * (factorial(n - 1))
    }
    
    main() : Any
    {
        factorial(10)
    }
}

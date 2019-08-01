/********************************************************
*                                                       *
*   Copyright (C) Microsoft. All rights reserved.       *
*                                                       *
********************************************************/

module Types1
{
    Main() : {{Num:Integer32; Flag:Logical;}*}
    {
        (from n in {5, 4, 0}
        where n < 5
        select {Num =>n, Flag =>(n>0)}):{{Num:Integer32; Flag:Logical;}*}
    }
}

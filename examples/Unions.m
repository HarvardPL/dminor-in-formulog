/********************************************************
*                                                       *
*   Copyright (C) Microsoft. All rights reserved.       *
*                                                       *
********************************************************/

module TaggedUnions
{
    type T_tt : {tag: {42}; bar: Integer32;};
    type T_ff : {tag: {43}; foo: Text;};
    type U : T_tt | T_ff;

// this fails to typecheck, because it makes insufficient checks
// Test1(xs:{U*}) : {Text*} { from x in xs select x.foo  }
    
    Test2(xs : {U*}) : {Text*}
    {
        from x in xs
        select ( x.tag==42 ? "Hello" : x.foo )
    }

    Test3(xs : {U*}) : {Text*}
    {
        from x in xs
        where (x.tag==43)
        select x.foo
    }
}

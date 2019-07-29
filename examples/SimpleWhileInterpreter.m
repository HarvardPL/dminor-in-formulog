/********************************************************
*                                                       *
*   Copyright (C) Microsoft. All rights reserved.       *
*                                                       *
********************************************************/
module SimpleWhileInterpreter
{
/*
    type Operator : Text where
      value=="plus" || value=="minus" ||
      value=="times" //|| value=="div"
      || value=="<=";
    
    type Expression :
    {kind:{"variable"}; name: Text;} |
    {kind:{"integer"}; val: Integer32;} |
    {kind:{"binary app"}; operator: Operator;
      arg1: Expression; arg2: Expression;};
    

    type Statement :
    {kind:{"assignment"}; var: Text; rhs: Expression;} |
    {kind:{"while"}; test:Expression; body:Statement;} |
    {kind:{"if"}; test:Expression; tt:Statement; ff:Statement;} |
    {kind:{"seq"}; s1:Statement; s2:Statement;} |
    {kind:{"skip"};};
*/

    type Store : {{name:Text; val:Integer32;}*};

/*	    
    Var(name:Text) : Expression { {kind=>"variable", name =>name} }    
    Int(val : Integer32) : Expression { {kind=>"integer", val=>val} }    
    Plus(arg1 : Expression, arg2 : Expression) : Expression { {kind=>"binary app", operator=>"plus", arg1=>arg1, arg2=>arg2} }
    Minus(arg1 : Expression, arg2 : Expression) : Expression { {kind=>"binary app", operator=>"minus", arg1=>arg1, arg2=>arg2} }
    Times(arg1 : Expression, arg2 : Expression) : Expression { {kind=>"binary app", operator=>"times", arg1=>arg1, arg2=>arg2} }
    Leq(arg1 : Expression, arg2 : Expression) : Expression { {kind=>"binary app", operator=>"<=", arg1=>arg1, arg2=>arg2} }


    Assign(var: Text, rhs: Expression) : Statement { {kind=>"assignment", var => var, rhs => rhs} }
    While(test:Expression,body:Statement) : Statement { {kind=>"while", test=>test, body=>body} }
    If(test:Expression,tt:Statement,ff:Statement) : Statement { {kind=>"if", test=>test, tt=>tt, ff=>ff} }
    Seq(s1 : Statement, s2 : Statement) : Statement { {kind=>"seq", s1=>s1, s2=>s2} }
    Skip() : Statement { {kind=>"skip"} }


    BreakSingleton(xs : Store where value.Count == 1) : Integer32 {
        from x in xs let n = 0 : Integer32 accumulate x.val
    }

    
    // Non-termination is maybe not the best way to signal errors
    Error() : Any where false { Error() }
*/
    Lookup (st:Store, n:Text) : Any /*Integer32*/
    {
        let match = (from x in st where x.name == n select x) in
		match.Count /* == 1 */ /*(match.Count == 1) ? 42 : 21*/
/*
		(match.Count == 1) ? BreakSingleton(match) : Error()
*/
    }
    
 /*   
    Evaluate(e:Expression, st:Store) : Integer32
    {
       (e.kind == "variable") ? Lookup(st,e.name) : ( 
        (e.kind == "integer")  ? (e.val) : ( 
        (e.kind == "binary app") ? 
         ((e.operator=="plus") ? Evaluate(e.arg1,st) + Evaluate(e.arg2,st) : (
          (e.operator=="minus") ? Evaluate(e.arg1,st) - Evaluate(e.arg2,st) : (
          (e.operator=="times") ? Evaluate(e.arg1,st) * Evaluate(e.arg2,st) : (
          (e.operator=="<=") ?
            (let x1 = Evaluate(e.arg1,st) in
             let x2 = Evaluate(e.arg2,st) in
                (x1 < x2 || x1 == x2)? 1 : 0)
            : "unreachable" //unrecognized operator
         )))
         )
       : (
        "unreachable" //unrecognized expression form
        )))
    }
    
    UpdateStore(x:Text, n:Integer32, st:Store) : Store
    {
        // first remove the previous binding for x
        let st1 = (from y in st where x != y.name select y) in
        // then add a new one
        {{name=>x, val=>n}} ++ st1
    }
    
    Interpret(s:Statement, st:Store) : Store
    {
        (s.kind == "assignment") ? 
            (let rhs = Evaluate(s.rhs, st) in
            UpdateStore(s.var, rhs, st)
        ) : (
            (s.kind == "while") ?
                (let t = Evaluate(s.test, st) in 
                    (t == 0) ? st : (
                        Interpret(Seq(s.body,s), st)
                    )
            ) : (
                (s.kind == "if") ?
                    (let t = Evaluate(s.test, st) in 
                        (t == 0) ? Interpret(s.ff, st) : Interpret(s.tt, st)
                ) : (
                    (s.kind == "seq") ?
                        (let st1 = Interpret(s.s1, st) in 
                            Interpret(s.s2, st1)
                    ) : (
                        st // skip
                    )        
                )
            )
        )
    }
        
    Fact10() : Statement {
        Seq(Assign("f", Int(1)),
        Seq(Assign("i", Int(1)),
        While(Leq(Var("i"), Int(10)),
            Seq(Assign("f", Times(Var("f"),Var("i"))),
            Assign("i", Plus(Var("i"), Int(1)))))))
    }

    main() : Any
    {
        let res = Interpret(Fact10(), {}) in
            Evaluate(Var("f"),res)
    }
*/
}

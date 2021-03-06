%CALCULADORA%

:- use_module(library(pce)).

suma(N1,N2,R):- atom_number(N1,N1a), atom_number(N2,N2a), Result is N1a+N2a, send(R,selection,Result).
restar(N1,N2,R):- atom_number(N1,N1a), atom_number(N2,N2a), Result is N1a-N2a, send(R,selection,Result).
multiplicacion(N1,N2,R):- atom_number(N1,N1a), atom_number(N2,N2a), Result is N1a*N2a, send(R,selection,Result).
division(N1,N2,R):- atom_number(N1,N1a), atom_number(N2,N2a), Result is N1a/N2a, send(R,selection,Result).


main:- new(D, dialog('Calculadora')),
       new(Etiqueta, label(nombre,'Calculadora')),
       new(Salir, button('SALIR', message(D, destroy))),
       new(Numero1, text_item('Ingrese el primer numero')),
       new(Numero2, text_item('Ingrese el segundo numero')),
       new(Resultado, text_item('RESULTADO')),
       new(Suma, button('SUMA', message(@prolog,suma,Numero1?selection,Numero2?selection, Resultado))),
       new(Restar, button('RESTAR', message(@prolog,restar,Numero1?selection,Numero2?selection, Resultado))),
       new(Multiplicar, button('MULTIPLICAR', message(@prolog,multiplicacion,Numero1?selection,Numero2?selection, Resultado))),
       new(Dividir, button('DIVIDIR', message(@prolog,division,Numero1?selection,Numero2?selection, Resultado))),

       send(Resultado,editable,false),

       send(D, append, Etiqueta),
       send(D,append,Numero1),
       send(D,append,Numero2),
       send(D,append,Resultado),
       send(D,append,Suma),
       send(D,append,Restar),
       send(D,append,Multiplicar),
       send(D,append,Dividir),


       send(D,append,Salir),

       send(D,open).



public class Prueba {
	
	/*
	 * Prueba de entrenamiento del perceptron 
	 * para encontrar la funcion OR Logica
	 * 
	 */
	
	public static void main(String[] args) {
        Perceptron p = new Perceptron();
        //Salidas esperadas
        double[] objetivos = {1, 1, 1, -1};
        //Entradas
        //x1, x2, &#952;
        double[][] entradas = {
            {1, 1, -1},
            {1, -1, -1},
            {-1, 1, -1},
            {-1, -1, -1}
        };
        p.setEntradas(entradas);
        p.setObjetivos(objetivos);
        p.inicializarPesos();
        p.entrenar();
        System.out.println("********** Pesos Finales **********");
        p.imprimirPesos();
    }
}

public class Perceptron {
 
    private double[] pesos;
    private double[] objetivos;
    private double[][] entradas;
    private int numeroEntradas;
    private static final double TASA_APRENDIZAJE = 0.5d;
 
    //Constructor
    public Perceptron() {
    	
    }
    
    public double[][] getEntradas() {
        return entradas;
    }
 
    public void setEntradas(double[][] entradas) {
        this.entradas = entradas;
        this.numeroEntradas = entradas[0].length;
    }
 
    public double[] getObjetivos() {
        return objetivos;
    }
 
    public void setObjetivos(double[] objetivos) {
        this.objetivos = objetivos;
    }
 
    public double[] getPesos() {
        return pesos;
    }
 
    public void setPesos(double[] pesos) {
        this.pesos = pesos;
    }
 
    /**
     * Iniciar los pesos sinapticos con numeros aleatorios de los intervalos, bipolar [-1, 1]
     */
    public void inicializarPesos() {
        pesos = new double[numeroEntradas];
        for (int i = 0; i < numeroEntradas; i++) {
            pesos[i] = Math.random();
        }
    }
 
    public void imprimirPesos() {
        for (int i = 0; i < numeroEntradas; i++) {
            System.out.println("W[" + i + "] = " + pesos[i]);
        }
    }
 
    /**
     * wj(k+1)=wj(k)+&#951;[z(k)&#8722;y(k)]xj(k), j =1,2,...,n+1
     */
    public void recalcularPesos(int posicionEntrada, double y) {
        for (int i = 0; i < pesos.length; i++) {
            pesos[i] = pesos[i] + TASA_APRENDIZAJE * (objetivos[posicionEntrada] - y) * entradas[posicionEntrada][i];
        }
    }
 
    public void entrenar() {
        int indice = 0;
        double yi = 0;
        while (indice < entradas.length) {
            double suma = 0;
            for (int i = 0; i < numeroEntradas; i++) {
                suma += (pesos[i] * entradas[indice][i]);//&#8721; x[i] * W[i] 
            }
            yi = suma >= 0 ? 1 : -1;
            if (yi == objetivos[indice]) {
                //Correcto
                for (int i = 0; i < numeroEntradas; i++) {
                    System.out.print(entradas[indice][i] + "t");
                }
                System.out.print(" => Esperada = " + objetivos[indice] + ", Calculada = " + yi + "n");
            } else {
                //Incorrecto
                for (int i = 0; i < numeroEntradas; i++) {
                    System.out.print(entradas[indice][i] + "t");
                }
                System.out.print(" => Esperada = " + objetivos[indice] + ", Calculada = " + yi + " [Error]n");
                System.out.println("Corrección de pesos");
                recalcularPesos(indice, yi);
                imprimirPesos();
                System.out.println("--");
                indice = -1;
            }
            indice++;
        }
    }
}
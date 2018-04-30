/*
 * Encog(tm) Java Examples v3.4
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-examples
 *
 * Copyright 2008-2017 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */

package implementacion;

import javax.swing.JFrame;

import java.awt.Canvas;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.encog.Encog;
import org.encog.EncogError;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.train.strategy.ResetStrategy;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.platformspecific.j2se.TrainingDialog;
import org.encog.platformspecific.j2se.data.image.ImageMLData;
import org.encog.platformspecific.j2se.data.image.ImageMLDataSet;
import org.encog.util.downsample.Downsample;
import org.encog.util.downsample.RGBDownsample;
import org.encog.util.downsample.SimpleIntensityDownsample;
import org.encog.util.simple.EncogUtility;

//import javafx.event.ActionEvent;

/**
 * Should have an input file similar to:
 * 
 * CreateTraining: width:16,height:16,type:RGB 
 * Input: image:./coins/dime.png, identity:dime 
 * Input: image:./coins/dollar.png, identity:dollar 
 * Input: image:./coins/half.png, identity:half dollar 
 * Input: image:./coins/nickle.png, identity:nickle 
 * Input: image:./coins/penny.png, identity:penny 
 * Input: image:./coins/quarter.png, identity:quarter 
 * Network: hidden1:100, hidden2:0
 * Train: Mode:console, Minutes:1, StrategyError:0.25, StrategyCycles:50 
 * Whatis: image:./coins/dime.png 
 * Whatis: image:./coins/half.png 
 * Whatis: image:./coins/testcoin.png
 * 
 */
public class ImageNeuralNetwork extends Canvas implements KeyListener{

	/*public ImageNeuralNetwork(){
		JOptionPane.showMessageDialog(null, "Ejecuto esa nueva clase");
	}*/
	
	class ImagePair {
		private final File file;
		private final int identity;

		public ImagePair(final File file, final int identity) {
			super();
			this.file = file;
			this.identity = identity;
		}

		public File getFile() {
			return this.file;
		}

		public int getIdentity() {
			return this.identity;
		}
	}

	public static void main(final String[] args) {
		
		//args[0] = "C:/Users/NkO/eclipse/jee-neon/eclipse/workspace/NeuralImage2/src/ejemplo.txt";
		//Carga el archivo descripcion
		try{
			File archivo=new File("src/recursos/ejemplo.txt");
			final FileInputStream fstream = new FileInputStream(archivo);
			final DataInputStream in = new DataInputStream(fstream);
			final BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String a;
			String b="";
			while ((a = br.readLine()) != null) {
				b+=a+"\n";
			}
			if(b!=""){
				JOptionPane.showMessageDialog(null, b);
			}
			final ImageNeuralNetwork program = new ImageNeuralNetwork();
			program.execute(archivo.getAbsolutePath());
			program.incializarVentana();
			program.inicializarButtons();
			program.eventosButtons();
		}catch(IOException e){
			JOptionPane.showMessageDialog(null, "No se reconocio la direccion");
			e.printStackTrace();
		}
		
		
		/*if (args.length < 1) {
			System.out
					.println("Must specify command file.  See source for format.");
		} else {
			try {
				final ImageNeuralNetwork program = new ImageNeuralNetwork();
				program.execute(args[0]);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}*/
		
		Encog.getInstance().shutdown();
	}

	private JFrame ventana;
	public static final int ANCHO_VENTANA = 240;
	public static final int ALTO_VENTANA = 135;
	public JButton btnAbrirArchivo;
	private File nuevaImagen;
	private int indiceNuevaImagen;
	
	private final List<ImagePair> imageList = new ArrayList<ImagePair>();
	private final Map<String, String> args = new HashMap<String, String>();
	private final Map<String, Integer> identity2neuron = new HashMap<String, Integer>();
	private final Map<Integer, String> neuron2identity = new HashMap<Integer, String>();
	private ImageMLDataSet training;
	private String line;
	private int outputCount;
	private int downsampleWidth;
	private int downsampleHeight;
	private BasicNetwork network;

	private Downsample downsample;
	
	public File archivo;
	public boolean filtro = false;
	
	public void incializarVentana(){
		//Crear la ventana y establecer sus propiedades
		ventana = new JFrame(); //Crear instancia de la ventana
		ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Terminar aplicacion cuando se de click en la X
		ventana.setSize(ANCHO_VENTANA, ALTO_VENTANA); //Establecer las dimensiones de la ventana
		ventana.setLocationRelativeTo(null); //Centrar ventana en el escritorio
		ventana.setResizable(false);
		//Ventana en fullscreen
		//ventana.setExtendedState(JFrame.MAXIMIZED_BOTH);
		//ventana.setUndecorated(true);
		ventana.setTitle("Proyecto Inteligencia Artificial"); //Definir el titulo de la ventana
		ventana.getContentPane().add(this); //Agregar el Canvas (lienzo) a la ventana
		ventana.setLayout(null);
		//ventana.setVisible(true); //Mostrar ventana
		
	}
	
	public void inicializarButtons(){
		ventana.setVisible(false);
		btnAbrirArchivo = new JButton("Abrir");
		btnAbrirArchivo.setBounds(80,40, 70, 30);
		ventana.add(btnAbrirArchivo);
		ventana.setVisible(true);
	}

	public void eventosButtons(){
		btnAbrirArchivo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				buscarImagen();	
			}
		});
	}
	
	public void buscarImagen(){
	    JFileChooser fileChooser = new JFileChooser();
	    /*
	    JFileChooser.ExtensionFilter extFilterJPG = new JFileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        JFileChooser.ExtensionFilter extFilterPNG = new JFileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);*/
        
	    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG files (*.png)", "PNG"));
	    
        indiceNuevaImagen = fileChooser.showOpenDialog(this);
        if(indiceNuevaImagen == fileChooser.APPROVE_OPTION){
        	nuevaImagen=fileChooser.getSelectedFile();
        	
        }
        habilitarFiltro();
        if (nuevaImagen!=null){
        	try{
        		analizarImagen(nuevaImagen);
        	}catch(IOException e){
        		JOptionPane.showMessageDialog(null, "La imagen localizada en la ruta:\n"
        	+ nuevaImagen.getAbsolutePath() + " ,\nNo pudo ser analizada");
        	}
        	//JOptionPane.showMessageDialog(null,nuevaImagen.getAbsolutePath());
		    //System.out.println(nuevaImagen.getAbsolutePath());
		    //txtFotoLocation.setText(archivoActual.getAbsolutePath());
        }
	}
	
	public void habilitarFiltro(){
		String evaluar = "";
		do{
			do{
				if(evaluar!=""){
					JOptionPane.showMessageDialog(null, "No se pudo reconocer la instruccion, "
							+ "intente de nuevo");
				}
				evaluar = JOptionPane.showInputDialog("Desea habilitar el filtro de deteccion "
						+ "de objetos extra�os?\n1) Si.\n2) No.");
			}while((isNumeric(evaluar)==false));
		}while((Integer.valueOf(evaluar)>2)||(Integer.valueOf(evaluar)<1));
		if(Integer.valueOf(evaluar)==2){
			filtro = false;
		}else{
			filtro = true;
		}
	}
	
	public boolean isNumeric(String cadena){
		try {
			Integer.parseInt(cadena);
			return true;
		} catch (NumberFormatException nfe){
			return false;
		}
	}
	
	public void analizarImagen(File file) throws IOException {
		//final Image img = ImageIO.read(file);
		final BufferedImage tiles = ImageIO.read(file);
		ArrayList<BufferedImage> sub_imagenes = new ArrayList<BufferedImage>();
		ArrayList<BufferedImage> sub_imagenes2 = new ArrayList<BufferedImage>();
		int circulos=0;
		int rectangulos=0;
		int triangulos=0;
		int hexagonos=0;
		int extranios=0;
		
		sub_imagenes = encontrarSubImagenes(tiles);
		
		for(int i=0;i<sub_imagenes.size();i++){
			sub_imagenes2 = encontrarSubImagenes(sub_imagenes.get(i));
			for(int j=0;j<sub_imagenes2.size();j++){
				//final ImageMLData input = new ImageMLData(img);
				final Image imagen = sub_imagenes2.get(j);
				final ImageMLData input = new ImageMLData(imagen);
				input.downsample(this.downsample, false, this.downsampleHeight,
						this.downsampleWidth, 1, -1);
				
				//Comienzo experimento
				
				final MLData output = this.network.compute(input);
				/*
				JOptionPane.showMessageDialog(null, "El primer elemento del experimento es:\n"
						+output.getData()[0]);
				JOptionPane.showMessageDialog(null, "El segundo elemento del experimento es:\n"
						+output.getData()[1]);
				JOptionPane.showMessageDialog(null, "El tercer elemento del experimento es:\n"
						+output.getData()[2]);
				JOptionPane.showMessageDialog(null, "El cuarto elemento del experimento es:\n"
						+output.getData()[3]);
				*/
				double sumaOutputs = 0;
				double highOutput = 0;
				
				for(int k=0;k<4;k++){
					if(output.getData()[k]<0){
						sumaOutputs=(sumaOutputs + output.getData()[k]);
					}else if(output.getData()[k]>0){
						if(output.getData()[k]>highOutput){
							highOutput=output.getData()[k];
						}
					}
				}
				
				double promedio = ((sumaOutputs)/3);
				promedio = (promedio + highOutput);
				/*
				JOptionPane.showMessageDialog(null, "Promedio salidas menores que cero:\n"
						+(sumaOutputs/3)+"\nValor mas alto:\n"+highOutput+"\nPromedio alternativo\n"
						+promedio);
				*/
				//Fin experimento
				
				if(filtro){
					if((sumaOutputs<=(-0.999999))&&(highOutput>=(0.999999))&&(promedio<=(0.000001))
							&&(promedio>=(-0.000001))){
						//contarFiguras(input, file);
						final int winner = this.network.winner(input);
						
						if(this.neuron2identity.get(winner).equals("circulo")){
							circulos+=1;
						}else if(this.neuron2identity.get(winner).equals("rectangulo")){
							rectangulos+=1;
						}else if(this.neuron2identity.get(winner).equals("triangulo")){
							triangulos+=1;
						}else if(this.neuron2identity.get(winner).equals("hexagono")){
							hexagonos+=1;
						}
						System.out.println("New Image: " + file.getAbsolutePath() + ", it seems to be: "
								+ this.neuron2identity.get(winner));
						JOptionPane.showMessageDialog(null, "La sub-imagen localizada en la ruta: \n" 
								+ file.getAbsolutePath() + " ,\nparece ser un: "
								+ this.neuron2identity.get(winner));
					}else{
						extranios+=1;
						System.out.println("New Image: " + file.getAbsolutePath() + ", it seems to be: "
								+ "Objeto extra�o");
						JOptionPane.showMessageDialog(null, "La sub-imagen localizada en la ruta: \n" 
								+ file.getAbsolutePath() + " ,\nparece ser un: "
								+ "Objeto extra�o");
					}
				}else{
					//contarFiguras(input, file);
					final int winner = this.network.winner(input);
					
					if(this.neuron2identity.get(winner).equals("circulo")){
						circulos+=1;
					}else if(this.neuron2identity.get(winner).equals("rectangulo")){
						rectangulos+=1;
					}else if(this.neuron2identity.get(winner).equals("triangulo")){
						triangulos+=1;
					}else if(this.neuron2identity.get(winner).equals("hexagono")){
						hexagonos+=1;
					}
					System.out.println("New Image: " + file.getAbsolutePath() + ", it seems to be: "
							+ this.neuron2identity.get(winner));
					JOptionPane.showMessageDialog(null, "La sub-imagen localizada en la ruta: \n" 
							+ file.getAbsolutePath() + " ,\nparece ser un: "
							+ this.neuron2identity.get(winner));
				}
			}
		}
		if(filtro){
			JOptionPane.showMessageDialog(null, "Figuras encontradas\nCirculos: "+circulos
					+"\nrectangulos: "+rectangulos+"\nTriangulos: "+triangulos+"\nHexagonos: "+hexagonos
					+"\nExtra�os: "+extranios);
		}else{
			JOptionPane.showMessageDialog(null, "Figuras encontradas\nCirculos: "+circulos
					+"\nrectangulos: "+rectangulos+"\nTriangulos: "+triangulos+"\nHexagonos: "+hexagonos);
		}
	}
	
	
	public ArrayList <BufferedImage> encontrarSubImagenes(BufferedImage tiles){
		ArrayList <BufferedImage> subImagenes = new ArrayList<BufferedImage>();
		try{
			BufferedImage tileWhite = ImageIO.read(getClass().getResource("/recursos/White.png"));
			BufferedImage tileNothing = ImageIO.read(getClass().getResource("/recursos/Nothing.png"));
			BufferedImage tile1 = tileWhite.getSubimage(0, 0, 1, 1);
			BufferedImage tile2 = tileNothing.getSubimage(0, 0, 1, 1);
			ArrayList<Integer> straightVerticalLines = new ArrayList<Integer>();
			ArrayList<Integer> straightHorizontalLines = new ArrayList<Integer>();
			
			for(int x=0;x<tiles.getWidth();x++){
				for(int y=0;y<tiles.getHeight();y++){
					if((tile1.getRGB(0, 0)==tiles.getRGB(x, y))||
							(tile2.getRGB(0, 0)==tiles.getRGB(x, y))){
						if(y==(tiles.getHeight()-1)){
							straightVerticalLines.add(x);
						}
					}else{
						y=(tiles.getHeight()-1);
					}
				}
			}
			for(int y=0;y<tiles.getHeight();y++){
				for(int x=0;x<tiles.getWidth();x++){
					if((tile1.getRGB(0, 0)==tiles.getRGB(x, y))||
							(tile2.getRGB(0, 0)==tiles.getRGB(x, y))){
						if(x==(tiles.getWidth()-1)){
							straightHorizontalLines.add(y);
						}
					}else{
						x=(tiles.getWidth()-1);
					}
				}
			}
			
			ArrayList<Integer> verticesX = new ArrayList<Integer>();
			ArrayList<Integer> verticesY = new ArrayList<Integer>();
			
			if(straightVerticalLines.size()!=0){
				if(straightVerticalLines.get(0)!=0){
					verticesX.add(0);
					verticesX.add(straightVerticalLines.get(0));
				}
				//int straightLineYTemp=0;
				for(int i=1;i<straightVerticalLines.size();i++){
					if((straightVerticalLines.get((i-1)))!=(straightVerticalLines.get(i)-1)){
						verticesX.add(straightVerticalLines.get(i-1));
						verticesX.add(straightVerticalLines.get(i));
					}
				}
				
				if(straightVerticalLines.get(straightVerticalLines.size()-1)!=(tiles.getWidth()-1)){
					verticesX.add(straightVerticalLines.get(straightVerticalLines.size()-1));
					verticesX.add((tiles.getWidth()-1));
				}
			}else{
				verticesX.add(0);
				verticesX.add((tiles.getWidth()-1));
			}
			
			if(straightHorizontalLines.size()!=0){
				if(straightHorizontalLines.get(0)!=0){
					verticesY.add(0);
					verticesY.add(straightHorizontalLines.get(0));
				}
				//int straightLineYTemp=0;
				for(int i=1;i<straightHorizontalLines.size();i++){
					if((straightHorizontalLines.get((i-1)))!=(straightHorizontalLines.get(i)-1)){
						verticesY.add(straightHorizontalLines.get(i-1));
						verticesY.add(straightHorizontalLines.get(i));
					}
				}
				
				if(straightHorizontalLines.get(straightHorizontalLines.size()-1)!=(tiles.getHeight()-1)){
					verticesY.add(straightHorizontalLines.get(straightHorizontalLines.size()-1));
					verticesY.add((tiles.getHeight()-1));
				}
			}else{
				verticesY.add(0);
				verticesY.add((tiles.getHeight()-1));
			}
			
			subImagenes.clear();
			if((verticesX.size()!=0)&&(verticesY.size()!=0)){
				for(int x=1;x<verticesX.size();x+=2){
					for(int y=1;y<verticesY.size();y+=2){
						int pixelsX = (verticesX.get(x)-verticesX.get(x-1))+1;
						int pixelsY = (verticesY.get(y)-verticesY.get(y-1))+1;
						subImagenes.add(tiles.getSubimage(verticesX.get(x-1), verticesY.get(y-1), 
								pixelsX, pixelsY));
					}
				}
			}
			
		}catch(IOException e){
			JOptionPane.showMessageDialog(null, "No se han logrado leer todos o alguno de los "
					+ "tiles auxiliares ");
		}
		return subImagenes;
	}
	
	private int assignIdentity(final String identity) {

		if (this.identity2neuron.containsKey(identity.toLowerCase())) {
			return this.identity2neuron.get(identity.toLowerCase());
		}

		final int result = this.outputCount;
		this.identity2neuron.put(identity.toLowerCase(), result);
		this.neuron2identity.put(result, identity.toLowerCase());
		this.outputCount++;
		return result;
	}

	public void execute(final String file) throws IOException {
		final FileInputStream fstream = new FileInputStream(file);
		final DataInputStream in = new DataInputStream(fstream);
		final BufferedReader br = new BufferedReader(new InputStreamReader(in));

		while ((this.line = br.readLine()) != null) {
			executeLine();
		}
		in.close();
	}

	private void executeCommand(final String command,
			final Map<String, String> args) throws IOException {
		if (command.equals("input")) {
			processInput();
		} else if (command.equals("createtraining")) {
			processCreateTraining();
		} else if (command.equals("train")) {
			processTrain();
		} else if (command.equals("network")) {
			processNetwork();
		} else if (command.equals("whatis")) {
			processWhatIs();
		}

	}

	public void executeLine() throws IOException {
		final int index = this.line.indexOf(':');
		if (index == -1) {
			throw new EncogError("Invalid command: " + this.line);
		}

		final String command = this.line.substring(0, index).toLowerCase()
				.trim();
		final String argsStr = this.line.substring(index + 1).trim();
		final StringTokenizer tok = new StringTokenizer(argsStr, ",");
		this.args.clear();
		while (tok.hasMoreTokens()) {
			final String arg = tok.nextToken();
			final int index2 = arg.indexOf(':');
			if (index2 == -1) {
				throw new EncogError("Invalid command: " + this.line);
			}
			final String key = arg.substring(0, index2).toLowerCase().trim();
			final String value = arg.substring(index2 + 1).trim();
			this.args.put(key, value);
		}

		executeCommand(command, this.args);
	}

	private String getArg(final String name) {
		final String result = this.args.get(name);
		if (result == null) {
			throw new EncogError("Missing argument " + name + " on line: "
					+ this.line);
		}
		return result;
	}

	private void processCreateTraining() {
		final String strWidth = getArg("width");
		final String strHeight = getArg("height");
		final String strType = getArg("type");

		this.downsampleHeight = Integer.parseInt(strHeight);
		this.downsampleWidth = Integer.parseInt(strWidth);

		if (strType.equals("RGB")) {
			this.downsample = new RGBDownsample();
		} else {
			this.downsample = new SimpleIntensityDownsample();
		}

		this.training = new ImageMLDataSet(this.downsample, false, 1, -1);
		System.out.println("Training set created");
	}

	private void processInput() throws IOException {
		final String image = getArg("image");
		final String identity = getArg("identity");

		final int idx = assignIdentity(identity);
		final File file = new File(image);

		this.imageList.add(new ImagePair(file, idx));

		System.out.println("Added input image:" + image);
	}

	private void processNetwork() throws IOException {
		System.out.println("Downsampling images...");
		ArrayList<BufferedImage> sub_imagen = new ArrayList<BufferedImage>();
		
		for (final ImagePair pair : this.imageList) {
			final MLData ideal = new BasicMLData(this.outputCount);
			final int idx = pair.getIdentity();
			for (int i = 0; i < this.outputCount; i++) {
				if (i == idx) {
					ideal.setData(i, 1);
				} else {
					ideal.setData(i, -1);
				}
			}
			sub_imagen.clear();
			sub_imagen = encontrarSubImagenes(ImageIO.read(pair.getFile()));
			//final Image img = ImageIO.read(pair.getFile());
			final Image img = sub_imagen.get(0);
			final ImageMLData data = new ImageMLData(img);
			this.training.add(data, ideal);
		}

		final String strHidden1 = getArg("hidden1");
		final String strHidden2 = getArg("hidden2");
		//final String strHidden3 = getArg("hidden3");

		this.training.downsample(this.downsampleHeight, this.downsampleWidth);

		final int hidden1 = Integer.parseInt(strHidden1);
		final int hidden2 = Integer.parseInt(strHidden2);
		//final int hidden3 = Integer.parseInt(strHidden3);

		this.network = EncogUtility.simpleFeedForward(this.training
				.getInputSize(), hidden1, hidden2, 
				this.training.getIdealSize(), true);
		//this.network.addLayer(new BasicLayer(new ActivationSigmoid( ) , true , hidden3 ) );
		System.out.println("Created network: " + this.network.toString());
	}

	private void processTrain() throws IOException {
		final String strMode = getArg("mode");
		final String strMinutes = getArg("minutes");
		final String strStrategyError = getArg("strategyerror");
		final String strStrategyCycles = getArg("strategycycles");

		System.out.println("Training Beginning... Output patterns="
				+ this.outputCount);

		final double strategyError = Double.parseDouble(strStrategyError);
		final int strategyCycles = Integer.parseInt(strStrategyCycles);

		final ResilientPropagation train = new ResilientPropagation(this.network, this.training);
		train.addStrategy(new ResetStrategy(strategyError, strategyCycles));

		if (strMode.equalsIgnoreCase("gui")) {
			TrainingDialog.trainDialog(train, this.network, this.training);
		} else {
			final int minutes = Integer.parseInt(strMinutes);
			EncogUtility.trainConsole(train, this.network, this.training,
					minutes);
		}
		System.out.println("Training Stopped...");
	}

	public void processWhatIs() throws IOException {
		final String filename = getArg("image");
		final File file = new File(filename);
		ArrayList<BufferedImage> sub_imagen = new ArrayList<BufferedImage>();
		
		sub_imagen.clear();
		sub_imagen = encontrarSubImagenes(ImageIO.read(file));
		
		final Image img = sub_imagen.get(0);
		//final Image img = ImageIO.read(file);
		final ImageMLData input = new ImageMLData(img);
		input.downsample(this.downsample, false, this.downsampleHeight,
				this.downsampleWidth, 1, -1);
		final int winner = this.network.winner(input);
		System.out.println("What is: " + filename + ", it seems to be: "
				+ this.neuron2identity.get(winner));
	}
	/*
	 		@FXML
	public void BuscarFoto(ActionEvent event) {
	    FileChooser fileChooser = new FileChooser();
	    
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
        archivoActual = fileChooser.showOpenDialog(main.getStageFoto());
        if (archivoActual!=null){
		    System.out.println(archivoActual.getAbsolutePath());
		    txtFotoLocation.setText(archivoActual.getAbsolutePath());
        }
	}
	 */

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}

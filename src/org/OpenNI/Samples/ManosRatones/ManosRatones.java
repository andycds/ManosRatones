package org.OpenNI.Samples.ManosRatones;

import java.awt.Graphics;

import javax.swing.JFrame;

import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMap;
import org.OpenNI.DepthMetaData;
import org.OpenNI.ErrorStateEventArgs;
import org.OpenNI.HandsGenerator;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.NodeType;
import org.OpenNI.OutArg;
import org.OpenNI.ScriptNode;
import org.OpenNI.Samples.SimpleRead.SimpleRead;

public class ManosRatones extends JFrame implements IObserver<ErrorStateEventArgs> {
	private static final long serialVersionUID = 1L;
	public static final String SAMPLES_XML = "./cfg/SamplesConfig.xml";
	int xRes = 0, yRes = 0;
	int x, y;
	int framesToDelete = 0;
	int pontos[][] = new int[10][2];
	int ponteiro = 0;

	public static void main(String[] args) {
		ManosRatones misManosSonRatones = new ManosRatones();
		misManosSonRatones.executar();
	}
	
	public void paint(Graphics g) {
		if (++framesToDelete > 50) {
			super.paint(g);
			framesToDelete = 0;
		}
		pontos[ponteiro][0] = x;
		pontos[ponteiro++][1] = y;
		if (ponteiro >= pontos.length) ponteiro = 0;
//		super.paint(g);
//		g.setColor(Color.GREEN);
		System.out.printf("------------ x: %d, y: %d -------------------------------\n", x, y);
		
		desenharCursor(g);
	}

	private void desenharCursor(Graphics g) {
		int xT = 0, yT = 0;
		for (int i = 0; i < pontos.length; i++) {
			xT += pontos[i][0];
			yT += pontos[i][1];
		}
		g.drawOval(xT / pontos.length, yT / pontos.length, 51, 51);
		g.drawOval(xT / pontos.length, yT / pontos.length, 50, 50);
	}
	
	public void executar() {
		
		try {
			OutArg<ScriptNode> scriptNodeArg = new OutArg<ScriptNode>();
			Context context = Context.createFromXmlFile(SAMPLES_XML, scriptNodeArg);

			SimpleRead pThis = new SimpleRead();

			context.getErrorStateChangedEvent().addObserver(pThis);

			DepthGenerator depth = (DepthGenerator)context.findExistingNode(NodeType.DEPTH);

			DepthMetaData depthMD = new DepthMetaData();
			
//			HandsGenerator hands = (HandsGenerator)context.findExistingNode(NodeType.HANDS);
//			hands.g
			
			setTitle("Manos Ratones - sus manos son ratones");
			context.waitAnyUpdateAll();
			depth.getMetaData(depthMD);
			xRes = depthMD.getXRes();
			yRes = depthMD.getYRes();
			System.out.printf("Resolu��o %d x %d", xRes, yRes);
			setSize(xRes, yRes);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setVisible(true);
			
			while (true) {
				context.waitAnyUpdateAll();

				depth.getMetaData(depthMD);

//				System.out.printf("Frame %d Middle point is: %d.\n", depthMD.getFrameID(),
//						depthMD.getData().readPixel(xRes / 2, yRes / 2));
				processData(depthMD.getData());
				paint(this.getGraphics());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void processData(DepthMap depth) {
		int minDepth = depth.readPixel(xRes / 2, yRes / 2);
		int prof;
		x = xRes / 2;
		y = yRes / 2;
		for (int i = 0; i < xRes; i++) {
			for (int j = 0; j < yRes; j++) {
				prof = depth.readPixel(i, j);
				if (prof > 0 && prof < minDepth) {
					minDepth = prof;
					x = i;
					y = j;
				}
			}
		}
		System.out.printf("x:%d, y:%d, d:%d\t MD:%d\n", x, y, minDepth, depth.readPixel(xRes / 2, yRes / 2));
	}

	@Override
	public void update(IObservable<ErrorStateEventArgs> arg0, ErrorStateEventArgs arg1) {
		System.out.printf("Global error state has changed: %s", arg1.getCurrentError());
		System.exit(1);
	}
}

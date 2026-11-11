package pcinventory.ui.builder;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import pcinventory.ui.AppPage;
import pcinventory.ui.MainFrame;
import pcinventory.ui.builder.BuilderDemoData.Scenario;

/**
 * Generates literal Swing UI screenshots for the Use Case dossier.
 * Runs under xvfb-run in GitHub Actions; images are downloadable artifacts.
 */
public final class BuilderScreenshotGenerator {
    private BuilderScreenshotGenerator() {}

    private static BuilderPanel findBuilder(Component root) {
        if(root instanceof BuilderPanel p)return p;
        if(root instanceof Container c)
            for(Component child:c.getComponents()) {
                BuilderPanel found=findBuilder(child);
                if(found!=null)return found;
            }
        return null;
    }
    public static void main(String[] args)throws Exception{
        File dir=new File(args.length>0?args[0]:"ui-screenshots");
        if(!dir.isDirectory()&&!dir.mkdirs())
            throw new IllegalStateException("Cannot create "+dir.getAbsolutePath());
        SwingUtilities.invokeAndWait(()->{
            try {
                MainFrame frame=new MainFrame();
                frame.setSize(1560,1000);
                frame.setLocation(0,0);
                frame.setVisible(true);
                frame.showPage(AppPage.BUILDER);
                BuilderPanel builder=findBuilder(frame);
                if(builder==null)throw new IllegalStateException("BuilderPanel not mounted");
                for(Scenario scenario:Scenario.values()){
                    builder.showDemoScenario(scenario);
                    frame.validate();
                    frame.doLayout();
                    BufferedImage image=new BufferedImage(
                        frame.getWidth(),frame.getHeight(),BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics=image.createGraphics();
                    try {frame.printAll(graphics);}
                    finally {graphics.dispose();}
                    File file=new File(dir,scenario.name()+"_Builder.png");
                    if(!ImageIO.write(image,"png",file))
                        throw new IllegalStateException("Cannot write PNG");
                    System.out.println("SCREENSHOT "+file.getAbsolutePath());
                }
                frame.dispose();
            }catch(Exception e){throw new RuntimeException(e);}
        });
    }
}

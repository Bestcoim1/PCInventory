package pcinventory.ui.builder;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/** Save the actual Swing panel/dialog content as PNG for use-case documentation. */
public final class BuilderImageExport {
    private BuilderImageExport() {}
    public static void save(Component source,Component parent,String proposedName) {
        JFileChooser chooser=new JFileChooser();
        chooser.setDialogTitle("Lưu ảnh giao diện Use Case (PNG)");
        chooser.setSelectedFile(new File(proposedName+".png"));
        if(chooser.showSaveDialog(parent)!=JFileChooser.APPROVE_OPTION)return;
        File file=chooser.getSelectedFile();
        if(!file.getName().toLowerCase().endsWith(".png"))
            file=new File(file.getParentFile(),file.getName()+".png");
        if(file.exists()&&JOptionPane.showConfirmDialog(parent,
                "Ghi đè ảnh đã tồn tại?","Xác nhận",
                JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        int width=source.getWidth(),height=source.getHeight();
        if(width<1||height<1){
            JOptionPane.showMessageDialog(parent,"Chưa có kích thước giao diện để chụp.");
            return;
        }
        BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics();
        try { source.printAll(g); } finally { g.dispose(); }
        try {
            if(!ImageIO.write(image,"png",file))
                throw new IOException("No PNG writer installed.");
            JOptionPane.showMessageDialog(parent,
                "Đã lưu ảnh: "+file.getAbsolutePath(),
                "Xuất ảnh thành công",JOptionPane.INFORMATION_MESSAGE);
        } catch(IOException ex) {
            JOptionPane.showMessageDialog(parent,
                "Không thể lưu PNG: "+ex.getMessage(),
                "Lỗi xuất ảnh",JOptionPane.ERROR_MESSAGE);
        }
    }
}

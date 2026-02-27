import javax.swing.SwingUtilities;
import bizstock.ui.MainFrame;

public class MainBizStock {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

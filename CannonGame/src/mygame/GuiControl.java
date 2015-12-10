package mygame;

import mygame.enums.GameState;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import static mygame.ServerMain.app;

/**
 * Panel to control the server's game state.
 * @author Bjorn van der Laan (bjovan-5)
 */
public class GuiControl extends JFrame {

    private static JLabel stateLabel;

    GuiControl() {
        super();


        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        stateLabel = new JLabel();
        setStateLabel(ServerMain.server_state);
        panel.add(stateLabel);

        for (final GameState state : GameState.values()) {
            JButton button = new JButton(state.toString());

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    changeServerState(state);
                }
            });

            panel.add(button);
        }


        this.add(panel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public static void changeServerState(final GameState state) {
        app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                ServerMain.changeState(state);
                return null;
            }
        });
    }

    public static void setStateLabel(GameState state) {
        stateLabel.setText("Current state: " + state.toString());
    }
}
package stockbite;

import stockbite.ui.MainFrame;

import javax.swing.*;
import java.awt.Font;
import java.util.Collections;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) { }
            // use one consistent font everywhere
            for (Object key : Collections.list(UIManager.getDefaults().keys())) {
                if (key.toString().endsWith(".font")) UIManager.put(key, new Font("SansSerif", Font.PLAIN, 13));
            }
            // pastel blue + orange look
            java.awt.Color white = java.awt.Color.WHITE, dark = stockbite.ui.Theme.DARK;
            UIManager.put("Panel.background", stockbite.ui.Theme.BG);
            UIManager.put("OptionPane.background", white);
            UIManager.put("OptionPane.messageForeground", dark);
            UIManager.put("TabbedPane.selected", stockbite.ui.Theme.ORANGE);
            UIManager.put("TabbedPane.contentAreaColor", stockbite.ui.Theme.MINT_DK);
            UIManager.put("TabbedPane.light", stockbite.ui.Theme.MINT);
            UIManager.put("TabbedPane.highlight", stockbite.ui.Theme.MINT);
            UIManager.put("TabbedPane.shadow", stockbite.ui.Theme.MINT_DK);
            UIManager.put("TabbedPane.darkShadow", stockbite.ui.Theme.MINT_DK);
            UIManager.put("TabbedPane.focus", stockbite.ui.Theme.ORANGE);
            UIManager.put("TabbedPane.borderHightlightColor", stockbite.ui.Theme.MINT);
            UIManager.put("TextField.selectionBackground", stockbite.ui.Theme.MINT);
            UIManager.put("TextField.selectionForeground", dark);
            UIManager.put("ComboBox.selectionBackground", stockbite.ui.Theme.MINT);
            UIManager.put("ComboBox.selectionForeground", dark);
            UIManager.put("ScrollBar.thumb", stockbite.ui.Theme.MINT);
            UIManager.put("ScrollBar.track", stockbite.ui.Theme.BG);
            new MainFrame().setVisible(true);
        });
    }
}

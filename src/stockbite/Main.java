package stockbite;

import stockbite.ui.MainFrame;
import stockbite.ui.Theme;

import javax.swing.*;
import java.awt.Color;
import java.awt.Insets;
import java.util.Collections;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) { }

            // one consistent body font (Inter) everywhere
            for (Object key : Collections.list(UIManager.getDefaults().keys())) {
                if (key.toString().endsWith(".font")) UIManager.put(key, Theme.BASE);
            }

            // portal colors
            Color white = Color.WHITE, dark = Theme.DARK;
            UIManager.put("Panel.background", Theme.BG);
            UIManager.put("OptionPane.background", white);
            UIManager.put("OptionPane.messageForeground", dark);
            UIManager.put("TabbedPane.selected", Theme.ORANGE);
            UIManager.put("TabbedPane.contentAreaColor", Theme.MINT_DK);
            UIManager.put("TabbedPane.light", Theme.MINT);
            UIManager.put("TabbedPane.highlight", Theme.MINT);
            UIManager.put("TabbedPane.shadow", Theme.MINT_DK);
            UIManager.put("TabbedPane.darkShadow", Theme.MINT_DK);
            UIManager.put("TabbedPane.focus", Theme.ORANGE);
            UIManager.put("TabbedPane.borderHightlightColor", Theme.MINT);
            UIManager.put("TextField.selectionBackground", Theme.MINT);
            UIManager.put("TextField.selectionForeground", dark);
            UIManager.put("ComboBox.selectionBackground", Theme.MINT);
            UIManager.put("ComboBox.selectionForeground", dark);
            UIManager.put("ScrollBar.thumb", Theme.MINT);
            UIManager.put("ScrollBar.track", Theme.BG);

            // tab padding / spacing
            UIManager.put("TabbedPane.tabInsets", new Insets(12, 28, 12, 28));
            UIManager.put("TabbedPane.tabAreaInsets", new Insets(10, 24, 0, 24));
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(2, 0, 0, 0));

            new MainFrame().setVisible(true);
        });
    }
}

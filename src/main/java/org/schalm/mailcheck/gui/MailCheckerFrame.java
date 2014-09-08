package org.schalm.mailcheck.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.Set;
import javax.mail.MessagingException;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import javax.xml.bind.JAXB;
import org.apache.log4j.Logger;
import org.schalm.mailcheck.ApplicationPreferences;
import org.schalm.mailcheck.Configuration;
import org.schalm.mailcheck.MailAccount;
import org.schalm.mailcheck.MailAccount.StateChangeType;
import org.schalm.mailcheck.MailMessage;
import org.schalm.mailcheck.NewMailAlert;
import org.schalm.util.helper.app.SizeHelper;
import org.schalm.util.helper.app.TableColumnPrefsHelper;
import org.schalm.util.helper.file.ExampleFileFilter;
import org.schalm.util.helper.file.FileHelper;
import org.schalm.util.helper.log.InMemoryLogger;
import org.schalm.util.helper.string.StringHelper;
import org.schalm.util.helper.swing.ToolTipTable;

/**
 * Frame for this application.
 *
 * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
 * @version $Id: MailCheckerFrame.java 168 2014-08-25 16:14:51Z cschalm $
 */
public final class MailCheckerFrame extends JFrame implements Observer {
    private static final long serialVersionUID = 6046514124548094000L;
    private static final Logger log = Logger.getLogger(MailCheckerFrame.class);
    private static final int checkPeriod = 15000;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMAN);
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.schalm.mailcheck.gui.messages");
    private static final String fileExtension = "jmc";
    private static final int HEIGHT_INITIAL = 27;
    private static final InMemoryLogger logger = new InMemoryLogger(sdf);
    private static MailCheckerFrame instance = null;
    private final File configFile;
    private String configDir;
    private String configFileName;
    public String uiConfigFileName;
    private SizeHelper sizeHelper = null;
    private TableColumnPrefsHelper tableHelper = null;
    private JTable tblMailBoxes = null;
    private MailAccountTableModel tblMailBoxesModel = null;
    private JTable tblMailMessages = null;
    private MailMessageTableModel tblMailMessagesModel = null;
    private Configuration configuration = new Configuration();
    private boolean hasUnsavedChanges = false;
    private JMenuItem deleteItem = null;
    private JMenuItem mailAccountPropertiesItem = null;
    private TrayIcon trayIcon = null;
    private final Image iconMailBoxEmpty = new ImageIcon(getClass().getResource("images/MailboxEmpty16.gif")).getImage();
    private final Image iconMailBoxFull = new ImageIcon(getClass().getResource("images/MailboxFull16.gif")).getImage();
    private boolean hasNewMessages = false;
    private NewMailAlert newMailAlert = null;
    private JLabel statusBar = null;
    private Timer timer = null;

    private MailCheckerFrame() {
        super(messages.getString("app.name"));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                setVisible(false);
            }

        });
        showSplash(2000);
        init();

        java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userRoot().node("org.schalm.jmailchecker");
        configDir = preferences.get("configdir", System.getProperty("user.home") + FileHelper.FILE_SEPARATOR + ".JMailChecker"
                + FileHelper.FILE_SEPARATOR);
        new File(configDir).mkdirs();
        configFileName = preferences.get("configfilename", "config." + MailCheckerFrame.fileExtension);
        configFile = new File(configDir + configFileName);
        uiConfigFileName = configDir + "uiConfig.xml";
        loadConfig();

        sizeHelper = new SizeHelper(this);
        sizeHelper.setConfigDir(configDir);
        sizeHelper.restoreSize();
        tableHelper = new TableColumnPrefsHelper(uiConfigFileName);
        tableHelper.addTable(new TableColumnPrefsHelper.TableConfig("mailBoxes", tblMailBoxes));
        tableHelper.addTable(new TableColumnPrefsHelper.TableConfig("mailMessages", tblMailMessages));

        JSplitPane splitPane = (JSplitPane) getContentPane().getComponent(0);
        JScrollPane spBox = (JScrollPane) splitPane.getLeftComponent();
        int rowCount = tblMailBoxesModel.getRowCount();
        int height = HEIGHT_INITIAL + tblMailBoxes.getRowHeight() * rowCount;
        spBox.setPreferredSize(new Dimension(500, height));
        loadUiConfig();
        logger.log("Starting " + messages.getString("app.name"));
        setVisible(true);

        checkAllNow();
    }

    public static synchronized MailCheckerFrame getInstance() {
        if (MailCheckerFrame.instance == null) {
            MailCheckerFrame.instance = new MailCheckerFrame();
        }
        return MailCheckerFrame.instance;
    }

    private void init() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        tblMailBoxesModel = new MailAccountTableModel();
        tblMailBoxes = new ToolTipTable(tblMailBoxesModel);
//        tblMailBoxes.setAutoCreateRowSorter(true);
        TableRowSorter<MailAccountTableModel> maSorter = new TableRowSorter<>(tblMailBoxesModel);
        List<RowSorter.SortKey> maSortKeys = new ArrayList<>();
        maSortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING)); // initially sort by Mailbox-Name
        maSorter.setSortKeys(maSortKeys);
        tblMailBoxes.setDefaultRenderer(Long.class, new MailAccountSizeTableCellRenderer());
        tblMailBoxes.setRowSorter(maSorter);
        tblMailBoxes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblMailBoxes.setIntercellSpacing(new Dimension(3, 1));
        tblMailBoxes.getSelectionModel().clearSelection();
        ListSelectionModel rowSelectionModel = tblMailBoxes.getSelectionModel();
        rowSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();
                deleteItem.setEnabled(!listSelectionModel.isSelectionEmpty());
                mailAccountPropertiesItem.setEnabled(!listSelectionModel.isSelectionEmpty());
                tblMailMessagesModel.removeAllRows();
                int viewRow = tblMailBoxes.getSelectedRow();
                if (viewRow >= 0) {
                    int modelRow = tblMailBoxes.convertRowIndexToModel(viewRow);
                    MailAccount mailAccount = tblMailBoxesModel.getMailAccount(modelRow);
                    tblMailMessagesModel.addAllRows(mailAccount.getMessages());
                } else {
                    for (MailAccount mailAccount : configuration.getAccountsList()) {
                        tblMailMessagesModel.addAllRows(mailAccount.getMessages());
                    }
                }
                updateStatusbar();
            }

        });
        tblMailBoxes.addMouseListener(new MouseAdapter() {
            private int lastRow = -1;

            @Override
            public void mouseClicked(MouseEvent me) {
                Point origin = me.getPoint();
                int rowInView = tblMailBoxes.rowAtPoint(origin);
                if (rowInView == lastRow) {
                    // delete selection
                    tblMailBoxes.getSelectionModel().clearSelection();
                    lastRow = -1;
                } else {
                    lastRow = rowInView;
                }
                checkBoxPopupMenu(me);
            }

            @Override
            public void mousePressed(MouseEvent me) {
                checkBoxPopupMenu(me);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                checkBoxPopupMenu(me);
            }

        });

        tblMailMessagesModel = new MailMessageTableModel();
        tblMailMessages = new ToolTipTable(tblMailMessagesModel);
        TableRowSorter<MailMessageTableModel> sorter = new TableRowSorter<>(tblMailMessagesModel);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(4, SortOrder.DESCENDING)); // initially sort by date desc
        sorter.setSortKeys(sortKeys);
        tblMailMessages.setRowSorter(sorter);
        tblMailMessages.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tblMailMessages.setIntercellSpacing(new Dimension(3, 1));
        tblMailMessages.setDefaultRenderer(String.class, new MailMessageTableCellRenderer());
        tblMailMessages.setDefaultRenderer(Date.class, new MailMessageDateTableCellRenderer(sdf));
        tblMailMessages.setDefaultRenderer(Integer.class, new MailMessageSizeTableCellRenderer());
        tblMailMessages.addMouseListener(new MouseAdapter() {
            private int lastRow = -1;

            @Override
            public void mouseClicked(MouseEvent me) {
                Point origin = me.getPoint();
                int rowInView = tblMailMessages.rowAtPoint(origin);
                if (me.getClickCount() == 2) {
                    if (rowInView != -1) {
                        int rowInModel = tblMailMessages.convertRowIndexToModel(rowInView);
                        MailMessage message = (MailMessage) tblMailMessages.getModel().getValueAt(rowInModel, 6);
                        showPreview(message);
                    }
                } else {
                    if (rowInView == lastRow) {
                        // delete selection
                        tblMailMessages.getSelectionModel().clearSelection();
                        lastRow = -1;
                    } else {
                        lastRow = rowInView;
                    }
                    checkMessagePopupMenu(me);
                }
            }

            @Override
            public void mousePressed(MouseEvent me) {
                checkMessagePopupMenu(me);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                checkMessagePopupMenu(me);
            }

        });
        JScrollPane spBox = new JScrollPane(tblMailBoxes);
        spBox.setMinimumSize(new Dimension(300, 50));
        spBox.setPreferredSize(new Dimension(500, HEIGHT_INITIAL + tblMailBoxes.getRowHeight()));
        JScrollPane spMess = new JScrollPane(tblMailMessages);
        spMess.setMinimumSize(new Dimension(300, 100));
        spMess.setPreferredSize(new Dimension(500, 150));
        splitPane.setLeftComponent(spBox);
        splitPane.setRightComponent(spMess);

        createMainMenu();

        getContentPane().add(splitPane, BorderLayout.CENTER);
        statusBar = new JLabel(" ");
        getContentPane().add(statusBar, BorderLayout.SOUTH);
        initializeTray();
        timer = new Timer(checkPeriod, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timerEvent();
            }

        });
        timer.start();
    }

    private void timerEvent() {
        for (MailAccount mailAccount : configuration.getAccountsList()) {
            final MailAccount account = mailAccount;
            SwingWorker worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    account.setElapsedTime(checkPeriod);
                    return null;
                }

            };
            worker.execute();
        }
    }

    private void showPreview(final MailMessage mailMessage) {
        final TextDialog textWindow = new TextDialog(this);
        textWindow.setTitle(sdf.format(mailMessage.getDate()) + " - " + mailMessage.getSubject() + " - " + mailMessage.getFrom());
        SwingWorker worker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                String content = mailMessage.getMailAccount().getContent(mailMessage, configuration.getPreferences().isShowHeader());
                textWindow.setText(content);
                return null;
            }

        };
        worker.execute();
        textWindow.setVisible(true);
    }

    public void checkAllNow() {
        tblMailMessagesModel.removeAllRows();
        Map<String, List<MailAccount>> accountsPerHostMap = new HashMap<>();
        for (MailAccount mailAccount : configuration.getAccountsList()) {
            if (mailAccount.getPollPeriode() > 0) {
                List<MailAccount> list = accountsPerHostMap.get(mailAccount.getMailHost());
                if (list == null) {
                    list = new ArrayList<>();
                    accountsPerHostMap.put(mailAccount.getMailHost(), list);
                }
                list.add(mailAccount);
            }
        }
        Set<String> hostsSet = accountsPerHostMap.keySet();
        for (String hostName : hostsSet) {
            if (log.isDebugEnabled()) {
                log.debug("Starting check for host \"" + hostName + "\"");
            }
            List<MailAccount> nonParallelizeableHostsList = accountsPerHostMap.get(hostName);
            checkAccountsSequentially(nonParallelizeableHostsList);
        }
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        ExampleFileFilter filter = new ExampleFileFilter(fileExtension, messages.getString("fileType.description"));
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(new File(configDir));
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            configDir = chooser.getCurrentDirectory().toString() + FileHelper.FILE_SEPARATOR;
            configFileName = chooser.getSelectedFile().getName();
            saveConfigLocation();
            loadConfig();
        }
    }

    private void saveConfig() {
        JAXB.marshal(configuration, configFile);
        hasUnsavedChanges = false;
    }

    private void loadConfig() {
        hasUnsavedChanges = false;
        resetNewMailAlert();
        if (configFile.exists() && configFile.canRead()) {
            configuration = JAXB.unmarshal(configFile, Configuration.class);
            if (configuration.getAccountsList() != null) {
                tblMailMessagesModel.removeAllRows();
                tblMailBoxesModel.removeAllRows();
                for (MailAccount mailAccount : configuration.getAccountsList()) {
                    tblMailBoxesModel.addRow(mailAccount);
                    mailAccount.addObserver(this);
                }
            }
        }
    }

    private void saveAsFile() {
        JFileChooser chooser = new JFileChooser();
        ExampleFileFilter filter = new ExampleFileFilter(fileExtension, messages.getString("fileType.description"));
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(new File(configDir));
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            configDir = chooser.getCurrentDirectory().toString() + FileHelper.FILE_SEPARATOR;
            configFileName = chooser.getSelectedFile().getName();
            if (!configFileName.endsWith("." + fileExtension)) {
                configFileName = configFileName + "." + fileExtension;
            }
            saveConfigLocation();
            saveConfig();
        }
    }

    private void newFile() {
        configuration.getAccountsList().clear();
        tblMailMessagesModel.removeAllRows();
        tblMailBoxesModel.removeAllRows();
        hasNewMessages = false;
        hasUnsavedChanges = true;
        updateTray();
        updateStatusbar();
    }

    private void checkExit() {
        if (hasUnsavedChanges) {
            int result = JOptionPane.showConfirmDialog(this, messages.getString("saveChangesBeforeExit"),
                    messages.getString("saveChanges"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                saveConfig();
            } else if (result == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        exit();
    }

    private void exit() {
        timer.stop();
        sizeHelper.saveSize();
        tableHelper.save();
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            tray.remove(trayIcon);
        }
        System.exit(0);
    }

    public void addChanged(MailAccount mailAccount) {
        hasUnsavedChanges = true;
        if (configuration.getAccountsList().contains(mailAccount)) {
            int row = tblMailBoxesModel.getRowIndex(mailAccount);
            tblMailBoxesModel.removeRow(row);
            configuration.getAccountsList().remove(mailAccount);
        }
        configuration.getAccountsList().add(mailAccount);
        tblMailBoxesModel.addRow(mailAccount);
    }

    private void createMainMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(messages.getString("menu.file"));
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem newFileItem = new JMenuItem(messages.getString("menu.item.new"), KeyEvent.VK_N);
        newFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        newFileItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                newFile();
            }

        });
        JMenuItem openItem = new JMenuItem(messages.getString("menu.item.open"), KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                openFile();
            }

        });
        JMenuItem saveItem = new JMenuItem(messages.getString("menu.item.save"), KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                saveConfig();
            }

        });
        JMenuItem saveAsItem = new JMenuItem(messages.getString("menu.item.saveAs"), KeyEvent.VK_A);
        saveAsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                saveAsFile();
            }

        });
        JMenuItem preferencesItem = new JMenuItem(messages.getString("menu.item.preferences"), KeyEvent.VK_P);
        preferencesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                showPreferences();
            }

        });
        JMenuItem exitItem = new JMenuItem(messages.getString("menu.item.exit"), KeyEvent.VK_E);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                checkExit();
            }

        });
        fileMenu.add(newFileItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(preferencesItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu(messages.getString("menu.edit"));
        editMenu.setMnemonic(KeyEvent.VK_E);
        JMenuItem newMailBoxItem = new JMenuItem(messages.getString("menu.item.newMailbox"), KeyEvent.VK_N);
        newMailBoxItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
        newMailBoxItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                newMailAccount();
            }

        });
        deleteItem = new JMenuItem(messages.getString("menu.item.delete"), KeyEvent.VK_N);
        deleteItem.setEnabled(false);
        deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                MailAccount mailAccount = getSelectedMailAccount();
                deleteMailAccount(mailAccount);
            }

        });
        mailAccountPropertiesItem = new JMenuItem(messages.getString("menu.item.properties"), KeyEvent.VK_P);
        mailAccountPropertiesItem.setEnabled(false);
        mailAccountPropertiesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                MailAccount mailAccount = getSelectedMailAccount();
                showMailAccountProperties(mailAccount);
            }

        });
        editMenu.add(newMailBoxItem);
        editMenu.add(deleteItem);
        editMenu.addSeparator();
        editMenu.add(mailAccountPropertiesItem);
        menuBar.add(editMenu);

        JMenu helpMenu = new JMenu(messages.getString("menu.help"));
        helpMenu.setMnemonic(KeyEvent.VK_H);
        JMenuItem aboutItem = new JMenuItem(messages.getString("menu.item.about"), KeyEvent.VK_A);
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                showSplash(0);
            }

        });
        helpMenu.add(aboutItem);
        JMenuItem viewLogItem = new JMenuItem(messages.getString("menu.item.viewLog"), KeyEvent.VK_L);
        viewLogItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                showLog();
            }

        });
        helpMenu.add(viewLogItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void newMailAccount() {
        MailAccount mailAccount = new MailAccount();
        mailAccount.addObserver(this);
        showMailAccountProperties(mailAccount);
    }

    private void showPreferences() {
        GeneralPreferencesDlg pd = new GeneralPreferencesDlg(this, configuration.getPreferences());
        pd.setLocationRelativeTo(this);
        pd.setVisible(true);
    }

    private void showMailAccountProperties(MailAccount mailAccount) {
        MailAccountPropertiesDlg pd = new MailAccountPropertiesDlg(logger, mailAccount);
        pd.setLocationRelativeTo(this);
        pd.setVisible(true);
    }

    private void checkMessagePopupMenu(MouseEvent me) {
        if (me.isPopupTrigger()) {
            Point origin = me.getPoint();
            int clickedRowInView = tblMailMessages.rowAtPoint(origin);
            if (tblMailMessages.getSelectionModel().isSelectionEmpty()
                    || !tblMailMessages.getSelectionModel().isSelectedIndex(clickedRowInView)) {
                tblMailMessages.getSelectionModel().setSelectionInterval(clickedRowInView, clickedRowInView);
            }
            int[] selectedRows = tblMailMessages.getSelectedRows();
            if (selectedRows != null && selectedRows.length > 0) {
                tblMailMessages.getSelectionModel().clearSelection();
                List<MailMessage> messageList = new ArrayList<>(selectedRows.length);
                for (int rowInView : selectedRows) {
                    tblMailMessages.getSelectionModel().addSelectionInterval(rowInView, rowInView);
                    int rowInModel = tblMailMessages.convertRowIndexToModel(rowInView);
                    MailMessage message = (MailMessage) tblMailMessages.getModel().getValueAt(rowInModel, 6);
                    messageList.add(message);
                }
                JPopupMenu popupMenu = new JPopupMenu();
                JMenuItem previewItem = new JMenuItem(messages.getString("menu.item.preview"));
                previewItem.addActionListener(new MailMessagePopupListener(messageList));
                JMenuItem deleteMessageItem = new JMenuItem(messages.getString("menu.item.delete"));
                deleteMessageItem.addActionListener(new MailMessagePopupListener(messageList));
                popupMenu.add(previewItem);
                popupMenu.addSeparator();
                popupMenu.add(deleteMessageItem);
                popupMenu.show(me.getComponent(), me.getX(), me.getY());
            }
        }
    }

    private void checkBoxPopupMenu(MouseEvent event) {
        if (event.isPopupTrigger()) {
            Point origin = event.getPoint();
            int rowInView = tblMailBoxes.rowAtPoint(origin);
            int rowInModel = tblMailBoxes.convertRowIndexToModel(rowInView);
            MailAccount selectedMailAccount = tblMailBoxesModel.getMailAccount(rowInModel);
            tblMailBoxes.getSelectionModel().clearSelection();
            tblMailBoxes.getSelectionModel().setSelectionInterval(rowInView, rowInView);
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem newItem = new JMenuItem(messages.getString("menu.item.new"));
            newItem.addActionListener(new MailAccountPopupListener(selectedMailAccount));
            JMenuItem deleteBoxItem = new JMenuItem(messages.getString("menu.item.delete"));
            deleteBoxItem.addActionListener(new MailAccountPopupListener(selectedMailAccount));
            JMenuItem checkItem = new JMenuItem(messages.getString("menu.item.checkNow"));
            checkItem.addActionListener(new MailAccountPopupListener(selectedMailAccount));
            JMenuItem propertiesItem = new JMenuItem(messages.getString("menu.item.properties"));
            propertiesItem.addActionListener(new MailAccountPopupListener(selectedMailAccount));
            popupMenu.add(newItem);
            popupMenu.addSeparator();
            popupMenu.add(deleteBoxItem);
            popupMenu.addSeparator();
            popupMenu.add(checkItem);
            popupMenu.addSeparator();
            popupMenu.add(propertiesItem);
            popupMenu.show(event.getComponent(), event.getX(), event.getY());
        }
    }

    private void loadUiConfig() {
        File uiConfig = new File(uiConfigFileName);
        if (uiConfig.exists() && uiConfig.canRead()) {
            tableHelper.load();
        } else {
            // fallback
            resetUiConfig();
        }
    }

    /*
     * Create a reasonable look of all tables depending on screensize.
     */
    private void resetUiConfig() {
        int totalWidth = Toolkit.getDefaultToolkit().getScreenSize().width - 2;
        int restWidth = totalWidth - 3 * 140;
        int columnWidth = (int) restWidth / 4;
        tblMailMessages.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblMailMessages.getColumnModel().getColumn(0).setPreferredWidth(140);
        tblMailMessages.getColumnModel().getColumn(1).setPreferredWidth(columnWidth);
        tblMailMessages.getColumnModel().getColumn(2).setPreferredWidth(columnWidth);
        tblMailMessages.getColumnModel().getColumn(3).setPreferredWidth(2 * columnWidth);
        tblMailMessages.getColumnModel().getColumn(4).setPreferredWidth(140);
        tblMailMessages.getColumnModel().getColumn(5).setPreferredWidth(140);
        tblMailBoxes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        restWidth = totalWidth - 140 - 50 - 2 * 80;
        columnWidth = (int) restWidth / 4;
        tblMailBoxes.getColumnModel().getColumn(0).setPreferredWidth(140);
        tblMailBoxes.getColumnModel().getColumn(1).setPreferredWidth(columnWidth);
        tblMailBoxes.getColumnModel().getColumn(2).setPreferredWidth(columnWidth);
        tblMailBoxes.getColumnModel().getColumn(3).setPreferredWidth(50);
        tblMailBoxes.getColumnModel().getColumn(4).setPreferredWidth(columnWidth);
        tblMailBoxes.getColumnModel().getColumn(5).setPreferredWidth(columnWidth);
        tblMailBoxes.getColumnModel().getColumn(6).setPreferredWidth(80);
        tblMailBoxes.getColumnModel().getColumn(7).setPreferredWidth(80);
    }

    /**
     * ActionListener for showing a context menu on a mailbox.
     *
     * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
     * @version $Id: MailCheckerFrame.java 168 2014-08-25 16:14:51Z cschalm $
     */
    private class MailAccountPopupListener implements ActionListener {
        private MailAccount selectedMailAccount = null;

        public MailAccountPopupListener(MailAccount selectedMailAccount) {
            this.selectedMailAccount = selectedMailAccount;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (messages.getString("menu.item.properties").equalsIgnoreCase(command)) {
                showMailAccountProperties(selectedMailAccount);
            } else if (messages.getString("menu.item.new").equalsIgnoreCase(command)) {
                newMailAccount();
            } else if (messages.getString("menu.item.checkNow").equalsIgnoreCase(command)) {
                checkAccount(selectedMailAccount);
            } else if (messages.getString("menu.item.delete").equalsIgnoreCase(command)) {
                deleteMailAccount(selectedMailAccount);
            }
        }

    }

    /**
     * ActionListener for showing a context menu on a mailmessage.
     *
     * @author <a href="mailto:cschalm@users.sourceforge.net">Carsten Schalm</a>
     * @version $Id: MailCheckerFrame.java 168 2014-08-25 16:14:51Z cschalm $
     */
    private class MailMessagePopupListener implements ActionListener {
        private List<MailMessage> selectedMessagesList = Collections.emptyList();

        public MailMessagePopupListener(List<MailMessage> selectedMessagesList) {
            this.selectedMessagesList = selectedMessagesList;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (messages.getString("menu.item.preview").equalsIgnoreCase(command)) {
                for (MailMessage mailMessage : selectedMessagesList) {
                    showPreview(mailMessage);
                }
            } else if (messages.getString("menu.item.delete").equalsIgnoreCase(command)) {
                if (configuration.getPreferences().isConfirmDelete()) {
                    String message = messages.getString("reallyDeleteMail");
                    if (selectedMessagesList.size() > 1) {
                        message = messages.getString("reallyDeleteMails");
                        message = MessageFormat.format(message, new Object[]{Integer.toString(selectedMessagesList.size())});
                    }
                    int result = JOptionPane.showConfirmDialog(MailCheckerFrame.getInstance(), message, messages.getString("confirmDelete"),
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        deleteMailMessages(selectedMessagesList);
                    }
                } else {
                    deleteMailMessages(selectedMessagesList);
                }
            }
        }

    }

    private void deleteMailAccount(MailAccount mailAccount) {
        int result = JOptionPane.showConfirmDialog(this, messages.getString("reallyDeleteMailbox"),
                messages.getString("confirmDeleteBox"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            logger.log("deleting mailAccount \"" + mailAccount.toString() + "\"");
            tblMailMessagesModel.removeAllRows4MailAccount(mailAccount);
            int row = tblMailBoxesModel.getRowIndex(mailAccount);
            tblMailBoxesModel.removeRow(row);
            hasUnsavedChanges = true;
            configuration.getAccountsList().remove(mailAccount);
        }
    }

    private void deleteMailMessages(List<MailMessage> toDeleteList) {
        StringBuilder sb = getMailMessagesText(toDeleteList);
        logger.log("deleting messages \"" + sb.toString() + "\"");
        Map<MailAccount, List<MailMessage>> concernedAccountsMap = new HashMap<>();
        for (MailMessage mailMessage : toDeleteList) {
            MailAccount mailAccount = mailMessage.getMailAccount();
            List<MailMessage> messageList = concernedAccountsMap.get(mailAccount);
            if (messageList == null) {
                messageList = new ArrayList<>();
            }
            messageList.add(mailMessage);
            concernedAccountsMap.put(mailAccount, messageList);
        }
        for (Map.Entry<MailAccount, List<MailMessage>> toDelete : concernedAccountsMap.entrySet()) {
            try {
                final Map.Entry<MailAccount, List<MailMessage>> toDeleteFinal = toDelete;
                SwingWorker worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        toDeleteFinal.getKey().deleteMailMessages(toDeleteFinal.getValue());
                        return null;
                    }

                };
                worker.execute();
            } catch (Exception e) {
                String message = messages.getString("error.deletingMail.message");
                String title = messages.getString("error.deletingMail.title");
                if (toDelete.getValue().size() > 1) {
                    message = messages.getString("error.deletingMails.message");
                    title = messages.getString("error.deletingMails.title");
                }
                StringBuilder mailsText = getMailMessagesText(toDelete.getValue());
                String messageText = MessageFormat.format(message,
                        new Object[]{mailsText.toString(), toDelete.getKey().toString(), e.getMessage()});
                logger.log(message, e);
                showErrorMessageDialog(title, messageText);
            }
        }
    }

    private void showSplash(final int period) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SplashScreen splash = new SplashScreen(MailCheckerFrame.getInstance());
                splash.showFor(period);
            }

        });
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    public void setPrefs(ApplicationPreferences prefs) {
        configuration.setPreferences(prefs);
        hasUnsavedChanges = true;
    }

    private void initializeTray() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            PopupMenu popup = new PopupMenu();
            MenuItem itemRestore = new MenuItem(messages.getString("menu.item.restore"));
            itemRestore.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MailCheckerFrame.getInstance().setVisible(true);
                    resetNewMailAlert();
                }

            });
            popup.add(itemRestore);
            MenuItem itemCheckAll = new MenuItem(messages.getString("menu.item.checkAll"));
            itemCheckAll.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    checkAllNow();
                }

            });
            popup.add(itemCheckAll);
            popup.addSeparator();
            MenuItem defaultItem = new MenuItem(messages.getString("menu.item.exit"));
            defaultItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    checkExit();
                }

            });
            popup.add(defaultItem);

            trayIcon = new TrayIcon(iconMailBoxEmpty, getNoOfMessagesText(0), popup);

            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MailCheckerFrame.getInstance().setVisible(true);
                    resetNewMailAlert();
                }

            });

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                log.error("TrayIcon could not be added: " + e.getMessage(), e);
            }

        } else {
            log.warn("System tray is currently not supported.");
        }
    }

    private void updateTray() {
        if (SystemTray.isSupported()) {
            int mailAmount = 0;
            for (MailAccount mailAccount : configuration.getAccountsList()) {
                mailAmount += mailAccount.getNoOfMails();
            }
            if (isVisible()) {
                hasNewMessages = false;
                trayIcon.setImage(mailAmount == 0 ? iconMailBoxEmpty : iconMailBoxFull);
            } else {
                // minimized, update blinking of trayIcon
                if (hasNewMessages) {
                    // trayIcon should blink
                    if (newMailAlert == null) {
                        newMailAlert = new NewMailAlert(trayIcon, iconMailBoxFull, iconMailBoxEmpty, 1.25f);
                        newMailAlert.setPriority(Thread.MIN_PRIORITY);
                    }
                    if (!newMailAlert.isAlive()) {
                        newMailAlert.start();
                    }
                } else {
                    // stop blinking
                    if (newMailAlert != null && newMailAlert.isAlive()) {
                        newMailAlert.cancel();
                    }
                    trayIcon.setImage(mailAmount == 0 ? iconMailBoxEmpty : iconMailBoxFull);
                }
            }
            trayIcon.setToolTip(getNoOfMessagesText(mailAmount));
        }
    }

    private void resetNewMailAlert() {
        for (MailAccount mailAccount : configuration.getAccountsList()) {
            mailAccount.resetNewMessages();
        }
        if (newMailAlert != null && newMailAlert.isAlive()) {
            newMailAlert.cancel();
        }
        hasNewMessages = false;
        updateTray();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof MailAccount) {
            if (isVisible()) {
                resetNewMailAlert();
            }
            MailAccount mailAccount = (MailAccount) o;
            if (arg != null && arg instanceof StateChangeType) {
                StateChangeType type = (StateChangeType) arg;
                switch (type) {
                    case MAILACCOUNT:
                        setMailAccountState(mailAccount);
                        break;
                    default:
                        setMailListState(mailAccount);
                        break;
                }
            }
        }
    }

    private void setMailListState(MailAccount mailAccount) {
        // update list of mails only if this MailAccount or none (=all) is selected
        boolean mailboxIsSelected = false;
        int viewRow = tblMailBoxes.getSelectedRow();
        if (viewRow >= 0) {
            int modelRow = tblMailBoxes.convertRowIndexToModel(viewRow);
            MailAccount selectedMailAccount = tblMailBoxesModel.getMailAccount(modelRow);
            if (selectedMailAccount.getUserName().equalsIgnoreCase(mailAccount.getUserName())) {
                mailboxIsSelected = true;
            }
        } else {
            mailboxIsSelected = true;
        }
        if (mailboxIsSelected) {
            // only mails of the selected MailAccount are visible
            tblMailMessagesModel.update(mailAccount);
            tblMailMessagesModel.fireTableDataChanged();
        }
        
        updateStatusbar();
        setMailAccountState(mailAccount);
        if (!hasNewMessages && !isVisible()) {
            hasNewMessages = mailAccount.hasNewMessages();
        }
        updateTray();
    }

    private void setMailAccountState(MailAccount mailAccount) {
        if (!hasNewMessages && !isVisible()) {
            hasNewMessages = mailAccount.hasNewMessages();
        }
        int row = tblMailBoxesModel.getRowIndex(mailAccount);
        tblMailBoxesModel.fireTableRowsUpdated(row, row);
    }

    private void updateStatusbar() {
        MailAccount selectedMailAccount = getSelectedMailAccount();
        StringBuilder statusText = new StringBuilder();
        long sizeOfSelection = 0;
        if (selectedMailAccount != null) {
            // one MailAccount
            statusText.append(getNoOfMessagesText(selectedMailAccount.getNoOfMails()));
            sizeOfSelection = selectedMailAccount.getMailBoxSize();
        } else {
            // all MailAccounts
            int mails = 0;
            for (MailAccount mailAccount : configuration.getAccountsList()) {
                sizeOfSelection += mailAccount.getMailBoxSize();
                mails += mailAccount.getNoOfMails();
            }
            statusText.append(getNoOfMessagesText(mails));
        }
        statusText.append(" - ").append(StringHelper.getSizeString(sizeOfSelection));

        statusBar.setText(" " + statusText.toString());
    }

    private void showErrorMessageDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showLog() {
        TextDialog textWindow = new TextDialog(this);
        textWindow.setTitle("Logfile");
        String content = logger.toString();
        textWindow.setText(content);
        textWindow.setVisible(true);
    }

    private String getNoOfMessagesText(int noOfMessages) {
        StringBuilder sb = new StringBuilder(Integer.toString(noOfMessages));
        sb.append(' ');
        sb.append(messages.getString(noOfMessages == 1 ? "message.singular" : "message.plural"));
        return sb.toString();
    }

    private void checkAccount(final MailAccount mailAccount) {
        SwingWorker worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                check(mailAccount);
                return null;
            }

        };
        worker.execute();
    }

    private MailAccount getSelectedMailAccount() {
        final int selectedRow = tblMailBoxes.getSelectedRow();
        if (selectedRow > -1) {
            final int rowInModel = tblMailBoxes.convertRowIndexToModel(selectedRow);
            MailAccount mailAccount = tblMailBoxesModel.getMailAccount(rowInModel);
            return mailAccount;
        }
        return null;
    }

    private StringBuilder getMailMessagesText(List<MailMessage> messagesList) {
        StringBuilder sb = new StringBuilder();
        for (MailMessage mailMessage : messagesList) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(mailMessage.toString());
        }
        return sb;
    }

    private void checkAccountsSequentially(final List<MailAccount> nonParallelizeableHostsList) {
        SwingWorker worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (MailAccount mailAccount : nonParallelizeableHostsList) {
                    check(mailAccount);
                }
                return null;
            }

        };
        worker.execute();
    }

    public void saveConfigLocation() {
        java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userRoot().node("org.schalm.jmailchecker");
        preferences.put("configdir", configDir);
        preferences.put("configfilename", configFileName);
    }

    private void check(final MailAccount mailAccount) {
        try {
            mailAccount.checkAccount();
        } catch (MessagingException me) {
            String message = MessageFormat.format(messages.getString("error.checkAccount.message"),
                    new Object[]{mailAccount.toString(), me.getMessage()});
            MailCheckerFrame.logger.log(message, me);
            showErrorMessageDialog(messages.getString("error.checkAccount.title"), message);
        }
    }

}

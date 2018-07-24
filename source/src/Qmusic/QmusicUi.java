package Qmusic;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

/**
 * 下载器UI
 *  Jlist参考文章: https://blog.csdn.net/xietansheng/article/details/74363719
 */
public class QmusicUi extends JFrame {

	private JPanel mainPane;
	private JTextField textKeyword;
	private Search search = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					QmusicUi frame = new QmusicUi();
					frame.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public QmusicUi() {
		setTitle("QQ Music Downloader");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 730, 400);
		mainPane = new JPanel();
		mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mainPane);
		mainPane.setLayout(null);

		textKeyword = new JTextField();
		textKeyword.setText("\u75C5\u53D8");
		textKeyword.setBounds(40, 10, 454, 25);
		mainPane.add(textKeyword);
		textKeyword.setColumns(10);

		JList<String> songList = new JList();
		songList.setBounds(40, 78, 377, 129);
		mainPane.add(songList);

		JButton btnSearch = new JButton("\u641C\u7D22");
		btnSearch.setBounds(505, 10, 95, 25);
		mainPane.add(btnSearch);

		JButton btnLastPage = new JButton("\u4E0A\u4E00\u9875");
		btnLastPage.setBounds(40, 45, 93, 23);
		mainPane.add(btnLastPage);
		btnLastPage.setEnabled(false);

		JButton btnNextPage = new JButton("\u4E0B\u4E00\u9875");
		btnNextPage.setBounds(165, 45, 93, 23);
		mainPane.add(btnNextPage);
		btnNextPage.setEnabled(false);

		JButton btnPlay = new JButton("\u4E0B\u8F7D&\u64AD\u653E");
		btnPlay.setBounds(290, 45, 100, 23);
		mainPane.add(btnPlay);
		btnPlay.setEnabled(false);

		JScrollPane scrollPane = new JScrollPane(songList);
		scrollPane.setBounds(40, 80, 560, 250);
		mainPane.add(scrollPane);

		JLabel countLabel = new JLabel("");
		countLabel.setHorizontalAlignment(SwingConstants.LEFT);
		countLabel.setBounds(530, 65, 80, 15);
		mainPane.add(countLabel);

		JLabel pageLabel = new JLabel("");
		pageLabel.setHorizontalAlignment(SwingConstants.LEFT);
		pageLabel.setBounds(530, 45, 80, 15);
		mainPane.add(pageLabel);

		JButton btnDownDir = new JButton("\u4E0B\u8F7D\u76EE\u5F55");
		btnDownDir.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btnDownDir.setToolTipText("");
		btnDownDir.setHorizontalAlignment(SwingConstants.LEFT);
		btnDownDir.setIcon(
				new ImageIcon(QmusicUi.class.getResource("/javax/swing/plaf/metal/icons/ocean/directory.gif")));
		btnDownDir.setBounds(605, 80, 105, 23);
		mainPane.add(btnDownDir);

		JLabel lblPowerBy = new JLabel("Power By");
		lblPowerBy.setBounds(632, 315, 54, 15);
		mainPane.add(lblPowerBy);

		JLabel lblWilo = new JLabel("WILO");
		lblWilo.setFont(new Font("汉仪蝶语体简", Font.PLAIN, 13));
		lblWilo.setHorizontalAlignment(SwingConstants.CENTER);
		lblWilo.setBounds(632, 330, 54, 15);
		mainPane.add(lblWilo);
		
		JLabel lblCprogramFileswindows = new JLabel("\u8C03\u7528\u7684\u5916\u90E8\u64AD\u653E\u5668\u8DEF\u5F84\u4E3A C:\\Program Files\\Windows Media Player\\wmplayer.exe");
		lblCprogramFileswindows.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
		lblCprogramFileswindows.setBounds(45, 340, 550, 15);
		mainPane.add(lblCprogramFileswindows);

		// 打开下载目录按钮监听事件
		btnDownDir.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String uri = System.getProperty("user.dir") + "\\Download\\";
				File file = new File(uri);
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				if (!file.exists()) {
					file.mkdir();
				}
				Runtime ru = Runtime.getRuntime();
				try {
					// 调用资源管理器打开目录
					ru.exec("explorer.exe " + "\"" + uri + "\"");
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		});

		// 搜索按钮监听事件
		btnSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					search = new Search(textKeyword.getText(), 1);
					search.debugShow();
					// 构造UI列表数据数组
					String listData[] = new String[search.js_curnum];
					int i = 0;
					// 将歌手-歌名值传至UI列表数据数组
					for (Media song : search.list) {
						listData[i] = song.singer + " - " + song.title;
						i++;
					}
					countLabel.setText("list: " + String.valueOf(search.js_curnum));
					songList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);			//只允许jlist单选
					songList.setListData(listData);											//传递数据数组至songList
					if (search.js_curpage < search.js_totalpage) {
						btnNextPage.setEnabled(true);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				pageLabel.setText("page: " + search.js_curpage + " / " + search.js_totalpage);
			}
		});
		
		//下一页按钮监听事件
		btnNextPage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					search = new Search(textKeyword.getText(), search.js_curpage + 1);
					String listData[] = new String[search.js_curnum];
					int i = 0;
					for (Media song : search.list) {
						listData[i] = song.singer + " - " + song.title;
						i++;
					}
					countLabel.setText("list: " + String.valueOf(search.js_curnum));
					songList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
					songList.setListData(listData);
					if (search.js_curpage < search.js_totalpage) {
						btnNextPage.setEnabled(true);
					} else {
						btnNextPage.setEnabled(false);
					}
					if (search.js_curpage > 1) {
						btnLastPage.setEnabled(true);
					} else {
						btnLastPage.setEnabled(false);
					}
					btnPlay.setEnabled(true);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				pageLabel.setText("page: " + search.js_curpage + " / " + search.js_totalpage);
			}
		});
		
		//上一页按钮监听事件
		btnLastPage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					search = new Search(textKeyword.getText(), search.js_curpage - 1);
					String listData[] = new String[search.js_curnum];
					int i = 0;
					for (Media song : search.list) {
						listData[i] = song.singer + " - " + song.title;
						i++;
					}
					countLabel.setText("list: " + String.valueOf(search.js_curnum));
					songList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
					songList.setListData(listData);
					if (search.js_curpage < search.js_totalpage) {
						btnNextPage.setEnabled(true);
					} else {
						btnNextPage.setEnabled(false);
					}
					if (search.js_curpage > 1) {
						btnLastPage.setEnabled(true);
					} else {
						btnLastPage.setEnabled(false);
					}
					btnPlay.setEnabled(true);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				pageLabel.setText("page: " + search.js_curpage + " / " + search.js_totalpage);
			}
		});
		
		//下载&播放按钮监听
		btnPlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Selected: " + songList.getSelectedIndex() + " ");
				search.list[songList.getSelectedIndex()].debugShow();
				
				//定义文件下载路径
				String uri = System.getProperty("user.dir") + "\\Download\\"
						+ search.list[songList.getSelectedIndex()].singer.replace(" / ", ",") + "-"
						+ search.list[songList.getSelectedIndex()].title + ".m4a";
				System.out.println("Temp directory: " + uri);			//For debug
				
				File file = new File(uri);
				//检查文件路径是否存在
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				if (!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				OutputStream output = null;
				BufferedOutputStream bufferedOutput = null;
				try {
					output = new FileOutputStream(file);
					bufferedOutput = new BufferedOutputStream(output);
					//调用http client下载音乐文件数据
					CloseableHttpClient httpClient = HttpClients.createDefault();
					HttpUriRequest httpUrlRe = new HttpGet(search.list[songList.getSelectedIndex()].getMeidaUrl());
					httpUrlRe.addHeader(new BasicHeader("Cookie", "qqmusic_fromtag=66"));					//设置cookie绕过验证
					try {
						CloseableHttpResponse response = httpClient.execute(httpUrlRe);
						if (response != null) {
							HttpEntity entity = response.getEntity();
							bufferedOutput.write(EntityUtils.toByteArray(entity));
							bufferedOutput.close();
							 //TODO 没找到合适的m4a音乐文件解析方法，只能调用外部播放器
							Runtime ru = Runtime.getRuntime();
							ru.exec("C://Program Files//Windows Media Player//wmplayer.exe \"" + uri + "\"");			//调用wmplayer播放文件
						}
						if (response != null) {
							response.close();
						}
						if (httpClient != null) {
							httpClient.close();
						}
					} catch (ClientProtocolException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		//列表选择项变动监听
		songList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if (songList.getSelectedIndex() + 1 != 0) {
					countLabel.setText("list: " + String.valueOf(songList.getSelectedIndex() + 1) + " / "
							+ String.valueOf(search.js_curnum));
					btnPlay.setEnabled(true);
				} else {
					btnPlay.setEnabled(false);
					countLabel.setText("list: " + String.valueOf(search.js_curnum));
				}
			}
		});
	}
}

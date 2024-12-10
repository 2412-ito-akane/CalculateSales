package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)

		//全ファイルをfilesに格納
		//ディレクトリのパス→args[0](実行構成で設定済み)
		File[] files = new File(args[0]).listFiles();

		//ArrayListにrcdファイルを格納するための変数
		ArrayList<File> rcdFiles = new ArrayList<>();

		//全てのファイル名(拡張子含む)を取得する
		for(int i=0;i < files.length;i++) {

			//ファイル名の判定をしたい
			if((files[i].getName()).matches("^[0-9]{8}.rcd$")) {

				//trueの時、Listに追加
				rcdFiles.add(files[i]);
			}
		}

		//rcdFilesから1個1個のファイルの情報を取得したい
		readFile()

		for (int i=0; i < rcdFiles.size();i++) {


				BufferedReader br = null;
				File rcdFile = new File(rcdFiles[i]);
				FileReader fr = new FileReader(rcdFile);
				br = new BufferedReader(fr);

				String line;

				while((line = br.readLine())!=null) {
					//改行ごとに配列に格納
					List<>

					System.out.println(br);
				}


		}





		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}
	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			//lineには1行分の情報が入る
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)

				//カンマで区切って配列に格納
				String[] items = line.split(",");

				//配列に格納されているか確認
				for (int i =0;i<items.length; i++) {
					System.out.println(i+":"+items[i]);
				}

				//支店が増えても自動で対応できるようにする
				//配列に格納されている[0]を、putメソッドの引数として使いたい
				int i = 0;
				while  (i<items.length) {
					//格納した要素の[0]と[1]を支店コードと支店名を保持するマップへ
					branchNames.put(items[0], items[1]);

					//格納した要素の[0]を支店コードと売上額を保持するマップへ
					branchSales.put(items[0], 0L);
					i++;
				}



//				System.out.println(branchNames);
//				System.out.println(line);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)

		return true;
	}

}

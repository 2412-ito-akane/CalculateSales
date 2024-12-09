package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
	private static final String RCDFILE_SERIAL_ERROR = "売上ファイル名が連番になっていません";
	private static final String SALEAMOUNT_DIGIT_ERROR = "合計金額が10桁を超えました";
	private static final String BRANCHCODE_NONE = "の支店コードが不正です";
	private static final String SALELIST_SIZE_ERROR = "のフォーマットが不正です";

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
				//falseを受け取るとメインメソッドにreturnを返す
				return;
			}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)

		//全ファイルをfilesに格納
		//ディレクトリのパス→args[0](実行構成で設定済み)
		File[] files = new File(args[0]).listFiles();

		//ArrayListにrcdファイルを格納するための変数
		ArrayList<File> rcdFiles = new ArrayList<>();

		//全てのファイル名(拡張子含む)を取得する
		for(int i = 0; i < files.length; i++) {

			//ファイル名の判定をしたい
			if(files[i].getName().matches("^[0-9]{8}.rcd$")) {

				//trueの時、Listに追加
				rcdFiles.add(files[i]);
			}
		}

		//エラー処理：rcdFilesが連番か確認
		//rcdFilesを昇順にソートする
		Collections.sort(rcdFiles);
		//(rcdFiles－1)回繰り返す
		for(int i = 0; i < rcdFiles.size() - 1; i++) {
			//ファイル名の頭8文字を切り取ってintにする
				int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
				int latter = Integer.parseInt(rcdFiles.get(i+1).getName().substring(0, 8));

				//差が1になるか確認
				if((latter - former) != 1) {
					//エラーメッセージ表示
					System.out.println(RCDFILE_SERIAL_ERROR);
					return;
				}
		}

		//rcdFilesから1個1個のファイルの情報を取得したい
		//rcdFilesの要素の分だけ読込を繰り返す
		//売上ファイルの読込
		BufferedReader br = null;
		for(int i = 0; i < rcdFiles.size(); i++) {

			//2行ごとにListに格納させるためのリストを作りたい
			ArrayList<String> saleList = new ArrayList<>();

			//try-catchを使う必要
			try {
				FileReader fr = new FileReader(rcdFiles.get(i));
				br = new BufferedReader(fr);

				//1行ずつ読込む
				String line;
				//次の行がなくなるまで読込
				while((line = br.readLine())!= null) {
					//読込んだ分をリストに加える
					saleList.add(line);
				}

				//エラー処理：売上ファイルのListの要素数が2になっているか
				if(saleList.size() != 2) {
					//要素が2ではない時、エラーメッセージをコンソールに表示
					System.out.println(rcdFiles.get(i).getName() + SALELIST_SIZE_ERROR);
					return;
				}

				//エラー処理：売上ファイルの支店コードがbranchNamesにあるか
				if(!branchNames.containsKey(saleList.get(0))) {
					//なかった時エラーメッセージをコンソールに表示
					System.out.println(rcdFiles.get(i).getName() + BRANCHCODE_NONE);
					return;
				}

				//売上saleList[1]だけ取り出す
				//Stringとして格納されているのでlongに変換する
				long fileSale = Long.parseLong(saleList.get(1));

				String branchCode = saleList.get(0);

				//取り出した売上金額fileSaleをbranchSalesのvalueと加算させる
				//計算結果はsaleAmountとする
				long saleAmount = fileSale + branchSales.get(branchCode);

				//計算結果が10桁を超えた場合
				if(saleAmount >= 10000000000L) {
					System.out.println(SALEAMOUNT_DIGIT_ERROR);
					return;
				}

				//計算結果saleAmountをbranchSalesに上書きしたい
				branchSales.put(branchCode, saleAmount);

			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}

		}

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
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
			//エラー処理：ファイルが存在していないとき
			if(!file.exists()) {
				//ture：ファイルが存在しないとき、メッセージをコンソールに表示する
				System.out.println(FILE_NOT_EXIST);
				//falseをreadFileメソッドに返すことで処理が止まる
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			//lineには1行分の情報が入る
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)

				//カンマで区切って配列に格納
				String[] items = line.split(",");

				//エラー処理：","で区切ることができているか、配列に要素が2つのみか
				//かつ、支店コード数字3桁が入っているか確認
				if((items.length != 2) || (!items[0].matches("^[0-9]{3}"))) {
					//要素が2つではない、支店コードが3桁ではない
					//メッセージをコンソールに表示
					System.out.println(FILE_INVALID_FORMAT);
					//処理を終える
					return false;
				}

				//支店が増えても自動で対応できるようにする
				//配列に格納されている[0]を、putメソッドの引数として使いたい
				for(int i = 0; i < items.length; i++) {
					//格納した要素の[0]と[1]を支店コードと支店名を保持するマップへ
					branchNames.put(items[0], items[1]);

					//格納した要素の[0]を支店コードと売上額を保持するマップへ
					branchSales.put(items[0], 0L);
				}

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

		//BufferedWriterをnullにする
		BufferedWriter bw = null;

		//try-catch文
		try {
			//branch.outをFileクラスのインスタンスにする
			File file = new File(path, fileName);
			//filewriterとbufferedwriterに引数を渡していき、writeメソッドを使えるようにする
			FileWriter fw = new FileWriter(file);
			//bwはすでに宣言しているので、「Bufferedwrite bw」からはじめる必要ない。再宣言しているように勘違いしてしまうため。
			bw = new BufferedWriter(fw);

			//拡張for文：キーの数だけwriteメソッドを繰り返す
			for(String key : branchNames.keySet()) {
				//keyのペアになっているvalueは？
				//各々変数を設定する
				String name = branchNames.get(key);
				long sales = branchSales.get(key);

				//取り出したvalueをbrounch.outへ書き込みたい
				//writeを使う
				bw.write(key + "," + name + "," + sales);
				bw.newLine();

			}
		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

}

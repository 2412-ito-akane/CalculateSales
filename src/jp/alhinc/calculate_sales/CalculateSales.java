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
	//商品定義ファイル名
	private static final String FILE_NAME_COMMODITY_LIST = "commodity.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";
	//商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String COMMODITY_FILE_NOT_EXIST = "商品定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String COMMODITY_FILE_INVALID_FORMAT = "商品定義ファイルのフォーマットが不正です";
	private static final String RCDFILE_SERIAL_ERROR = "売上ファイル名が連番になっていません";
	private static final String SALEAMOUNT_DIGIT_ERROR = "合計金額が10桁を超えました";
	private static final String BRANCHCODE_NONE = "の支店コードが不正です";
	private static final String SALELIST_SIZE_ERROR = "のフォーマットが不正です";

	//支店コードと商品コード、各々の正規表現
	private static final String BRANCH_REGULAR_EXPRESSION = "^[0-9]{3}$";
	private static final String COMMODITY_REGULAR_EXPRESSION = "^[A-Za-z0-9]{8}$";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		//エラー処理：コマンドライン引数が設定されているのか
		if(args.length != 1) {
			//コマンドライン引数が1つ設定されていない場合
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		//商品コードと商品名を保持するMap
		Map<String, String> commodityNames = new HashMap<>();
		//商品コードと売上金額を保持するMap
		Map<String, Long> commoditySales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales, BRANCH_REGULAR_EXPRESSION, FILE_NOT_EXIST, FILE_INVALID_FORMAT)) {
			//falseを受け取るとメインメソッドにreturnを返す
			return;
		}

		//商品定義ファイル読込
		if(!readFile(args[0], FILE_NAME_COMMODITY_LIST, commodityNames, commoditySales, COMMODITY_REGULAR_EXPRESSION, COMMODITY_FILE_NOT_EXIST, COMMODITY_FILE_INVALID_FORMAT)) {
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
			//getName()メソッドでは[ファイルとディレクトリの名前]を取得する
			//エラー処理：filesの要素が[ファイル]であることを確認するためisFile()を加える
			if(files[i].isFile() && files[i].getName().matches("^[0-9]{8}.rcd$")) {
				//trueの時、Listに追加
				rcdFiles.add(files[i]);
			}
		}

		//エラー処理：rcdFilesが連番か確認
		//rcdFilesを昇順にソートする
		Collections.sort(rcdFiles);
		//(rcdFiles－1)回繰り返す
		for(int i = 0; i < rcdFiles.size() -1; i++) {
			//ファイル名の頭8文字を切り取ってintにする
			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

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


				//saleList[0]は支店コード→branchCodeとして扱う
				String branchCode = saleList.get(0);

				//saleList[1]は商品コード→commodityCodeとして扱う
				String commodityCode = saleList.get(1);

				//saleList[2]は売上金額→saleValueとして扱う
				String saleValue = saleList.get(2);

				//エラー処理：売上ファイルのListの要素数が2になっているか
				//→商品ファイルが増えたので要素数は3になる
				if(saleList.size() != 3) {
					//要素が3ではない時、エラーメッセージをコンソールに表示
					System.out.println(rcdFiles.get(i).getName() + SALELIST_SIZE_ERROR);
					return;
				}

				//エラー処理：売上ファイルrcdFilesの支店コードbranchCodeがbranchNamesにあるか
				//商品コードcommodityCodeがcommodityNamesにあるかも確認したい
				if(!branchNames.containsKey(branchCode) || !commodityNames.containsKey(commodityCode)) {
					//なかった時エラーメッセージをコンソールに表示
					System.out.println(rcdFiles.get(i).getName() + BRANCHCODE_NONE);
					return;
				}

				//エラー処理：売上金額が数字か確認
				if(!saleValue.matches("^[0-9]+$")) {
					//数字ではないとき、エラーメッセージをコンソールに表示
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				//売上金額だけ取り出す
				//Stringとして格納されているのでlongに変換する
				long fileSale = Long.parseLong(saleValue);


				//取り出した売上金額fileSaleをbranchSalesのvalueと加算させる
				//計算結果はsaleAmountとする
				long saleAmount = fileSale + branchSales.get(branchCode);

				//取り出した売上金額fileSaleをcommoditySalesのvalueと加算させる
				//Map名.get(key)で商品コードに対応した金額を抽出する
				//計算結果はcommodityAmountとする
				long commodityAmount = fileSale + commoditySales.get(commodityCode);

				//エラー処理：計算結果が10桁を超えた場合
				//商品と支店それぞれを確認する
				if(saleAmount >= 10000000000L || commodityAmount >= 10000000000L) {
					System.out.println(SALEAMOUNT_DIGIT_ERROR);
					return;
				}

				//計算結果saleAmountをbranchSalesに上書きしたい
				//commodityAmountもcommoditySalesに上書きする
				branchSales.put(branchCode, saleAmount);
				commoditySales.put(commodityCode, commodityAmount);

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

		//商品別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_COMMODITY_OUT, commodityNames, commoditySales)) {
			return;
		}

	}


	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap +商品コードと商品名を保持するMap
	 * @param 支店コードと売上金額を保持するMap +商品コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> mapNames, Map<String, Long> mapSales, String regularExpression, String existError, String invalidFormat) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			//エラー処理：ファイルが存在していないとき
			if(!file.exists()) {
				//ture：ファイルが存在しないとき、メッセージをコンソールに表示する
				System.out.println(existError);
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
				//かつ、支店コードと商品コードが正規表現で入っているか確認
				if((items.length != 2) || (!items[0].matches(regularExpression))) {
					//要素が2つではない、支店コードが3桁ではない
					//メッセージをコンソールに表示
					System.out.println(invalidFormat);
					//処理を終える
					return false;
				}

				//支店と商品名が増えても自動で対応できるようにする
				//配列に格納されている[0]を、putメソッドの引数として使いたい
				//格納した要素の[0]と[1]を支店コードと支店名を保持するマップへ。商品名も同様
				mapNames.put(items[0], items[1]);

				//格納した要素の[0]を支店コード(または商品コード)と売上額を保持するマップへ
				mapSales.put(items[0], 0L);
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
	private static boolean writeFile(String path, String fileName, Map<String, String> mapNames, Map<String, Long> mapSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)

		//BufferedWriterをnullにする
		BufferedWriter bw = null;

		//try-catch文
		try {
			//branch.outをFileクラスのインスタンスにする
			//commodity.outも同様
			File file = new File(path, fileName);
			//filewriterとbufferedwriterに引数を渡していき、writeメソッドを使えるようにする
			FileWriter fw = new FileWriter(file);
			//bwはすでに宣言しているので、「Bufferedwrite bw」からはじめる必要ない。再宣言しているように勘違いしてしまうため。
			bw = new BufferedWriter(fw);

			//拡張for文：キーの数だけwriteメソッドを繰り返す
			for(String key : mapNames.keySet()) {
				//keyのペアになっているvalueは？→Map.get(キー名)で呼び出す
				//各々変数を設定する
				String name = mapNames.get(key);
				long sales = mapSales.get(key);

				//取り出したvalueをbranch.outとcommodity.outへ書き込みたい
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

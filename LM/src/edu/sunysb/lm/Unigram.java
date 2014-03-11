package edu.sunysb.lm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Unigram {

	HashMap<String, Integer> posMap = new HashMap<String, Integer>();
	HashMap<String, Integer> negMap = new HashMap<String, Integer>();
	HashMap<String, Double> posProb = new HashMap<String, Double>();
	HashMap<String, Double> negProb = new HashMap<String, Double>();
	int totalPosWords = 0;
	int totalNegWords = 0;
	HashMap<String, Count> fullMap = new HashMap<String, Count>();
	HashMap<String, Count> fullMapWithUnknown = new HashMap<String, Count>();
	HashMap<String, Probability> probWithSmoothing = new HashMap<String, Probability>();

	public static void main(String[] args) {
		String[] folders = { "txt_sentoken\\pos", "txt_sentoken\\neg" };
		for (int i = 0; i < 5; i++) {
			Unigram unigram = new Unigram();
			int start = i * 200;
			int end = start + 199;
			System.out.println("\nTest Data from: " + start + " - " + end);
			int totalTest = end - start + 1;
			unigram.directoryReader(folders[0], true, start, end);
			unigram.directoryReader(folders[1], false, start, end);
			unigram.fullMap = Helper.buildCumulativeMap(unigram.posMap,
					unigram.negMap);
			unigram.fullMapWithUnknown = Helper
					.buildUnknownMap(unigram.fullMap);
			unigram.probWithSmoothing = Helper
					.calcProbWithSmoothing(unigram.fullMapWithUnknown);

			int positiveSuccess = unigram.doClassify(folders[0], start, end,
					true);
			int negSuccess = unigram.doClassify(folders[1], start, end, false);
			System.out.println("Positives=" + positiveSuccess
					+ ", percent success: " + (positiveSuccess * 100.0)
					/ totalTest);
			System.out.println("Negatives=" + negSuccess
					+ ", percent success: " + (negSuccess * 100.0) / totalTest);
		}
	}

	/**
	 * Classifies file returns if file belongs to positive or negative class
	 *
	 * @param file
	 * @return
	 */
	public boolean classifyFile(File file) {
		String fileContents = Helper.fileReader(file);
		String[] wordList = fileContents.split(" ");
		double posProb = 0;
		double negProb = 0;
		for (int i = 0; i < wordList.length; i++) {
			Probability probObj;
			if (probWithSmoothing.containsKey(wordList[i])) {
				probObj = probWithSmoothing.get(wordList[i]);

			} else {
				probObj = probWithSmoothing.get(Helper.UNKNOWN);
			}
			negProb += probObj.negProb;
			posProb += probObj.posProb;
		}
		return (posProb > negProb);
	}

	/**
	 * Classifies test data and returns the success count
	 *
	 * @param dirName
	 * @param start
	 * @param end
	 * @param forPositive
	 * @return
	 */
	public int doClassify(String dirName, int start, int end,
			boolean forPositive) {
		File dirPath = new File(dirName);
		int successCount = 0;
		if (dirPath.isDirectory()) {
			File[] fileList = dirPath.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				if (i >= start && i <= end) {
					File child = fileList[i];
					if (classifyFile(child) == forPositive) {
						successCount++;
					}
				}
			}
		}
		return successCount;
	}

	public void directoryReader(String dirPath, boolean isPositiveDir,
			int start, int end) {
		File dir = new File(dirPath);
		if (dir.isDirectory()) {
			File[] fileList = dir.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				if (i >= start && i <= end) {
					continue;
				}
				File child = fileList[i];
				if (isPositiveDir) {
					fileReader(child, posMap);
				} else {
					fileReader(child, negMap);
				}

			}
		}
	}

	public void fileReader(File file, HashMap<String, Integer> map) {
		BufferedReader br = null;
		try {
			String line = null;
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			while ((line = br.readLine()) != null) {
				String parsedLine = Helper.cleanLine(line);
				String[] wordList = parsedLine.split(" ");
				for (int i = 0; i < wordList.length; i++) {
					if (map.containsKey(wordList[i]))
						map.put(wordList[i], map.get(wordList[i]) + 1);
					else
						map.put(wordList[i], 1);
				}
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
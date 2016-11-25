package com.shufflelunch.helper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author seisuke
 */
@Component
@Slf4j
public class EnvironmentHelper {
    @Autowired
    Environment environment;

    /**
     * 現在有効な実行環境のプロファイルに指定した環境が含まれているかどうかをチェックする。
     *
     * @param targetProfiles プロファイルに含まれているかどうか確認したい環境文字列のリスト
     *
     * @return 指定した環境が現在のプロファイルに含まれていればtrue。それ以外はfalse
     */
    public boolean includesProfiles(List<String> targetProfiles) {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        return !Collections.disjoint(activeProfiles, targetProfiles);
    }

    /**
     * ローカル環境かチェックする
     *
     * @return プロファイルがlocalであればtrue。それ以外はfalse
     */
    public boolean isLocal() {
        return includesProfiles(Collections.singletonList("local"));
    }
}
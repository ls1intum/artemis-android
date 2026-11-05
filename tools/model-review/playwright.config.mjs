import {defineConfig} from '@playwright/test';
export default defineConfig({testDir:'tests',testMatch:'**/*.spec.mjs',projects:[{name:'chromium',use:{browserName:'chromium'}},{name:'webkit',use:{browserName:'webkit'}}],workers:1,timeout:15000,reporter:[['line'],['json',{outputFile:'proof/browser-results.json'}]],use:{viewport:{width:800,height:600},headless:true}});

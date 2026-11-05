import {build} from 'esbuild';
import {mkdir,rm,copyFile,writeFile,readdir,readFile,stat} from 'node:fs/promises';
import {createHash} from 'node:crypto';
await mkdir('proof',{recursive:true});
await rm('dist',{recursive:true,force:true});await mkdir('dist/licenses',{recursive:true});
await build({entryPoints:['src/viewer.mjs'],bundle:true,minify:true,format:'iife',target:['safari16','chrome100'],outfile:'dist/artemis-model-review-1.0.0.js',legalComments:'linked',metafile:true,define:{'process.env.NODE_ENV':'"production"','Worker':'undefined'},logLevel:'info'}).then(async result=>{
 await writeFile('proof/bundle-metafile.json',JSON.stringify(result.metafile,null,2));
 const packages=new Set();
 for(const input of Object.keys(result.metafile.inputs)) {
  const match=input.match(/node_modules\/((?:@[^/]+\/)?[^/]+)/);if(match)packages.add(match[1]);
 }
 const notices=['Artemis model review 1.0.0 — built with official npm @tumaet/apollon 5.3.1.','Source: https://github.com/ls1intum/Apollon (MIT).','All dependencies are bundled locally. Embedded Inter font: SIL Open Font License 1.1.','No native tokens or network access are required.',''];
 for(const name of [...packages].sort()) {
  const directory=`node_modules/${name}`;const pkg=JSON.parse(await readFile(`${directory}/package.json`,'utf8'));
  notices.push(`${name} ${pkg.version} — ${pkg.license??'see license file'}`);
  for(const file of await readdir(directory)) if(/^(license|copying|notice)(\.|$|-)/i.test(file)) {
   if((await stat(`${directory}/${file}`)).isFile()) await copyFile(`${directory}/${file}`,`dist/licenses/${name.replaceAll('/','__')}-${file}`);
  }
 }
 // Apollon's bundled font license is supplied from the upstream source (retained in this source kit).
 await copyFile('licenses/Inter-OFL.txt','dist/licenses/Inter-OFL.txt');
 await writeFile('dist/THIRD-PARTY-NOTICES.txt',notices.join('\n')+'\n');
});
await copyFile('src/index.html','dist/index.html');
await copyFile('LICENSE','dist/LICENSE');
const manifest={version:'1.0.0',apollonVersion:'5.3.1',files:{}};
for(const file of await readdir('dist')) if((await stat(`dist/${file}`)).isFile()) {
 const bytes=await readFile(`dist/${file}`);manifest.files[file]={bytes:bytes.length,sha256:createHash('sha256').update(bytes).digest('hex')};
}
await writeFile('dist/manifest.json',JSON.stringify(manifest,null,2)+'\n');

# react-native-evosus-lou-pos

## Getting started

`$ npm install react-native-evosus-lou-pos --save`

### Mostly automatic installation

`$ react-native link react-native-evosus-lou-pos`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.evosus.loupos.EvosusLouPosPackage;` to the imports at the top of the file
  - Add `new EvosusLouPosPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-evosus-lou-pos'
  	project(':react-native-evosus-lou-pos').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-evosus-lou-pos/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-evosus-lou-pos')
  	```


## Usage
```javascript
import EvosusLouPos from 'react-native-evosus-lou-pos';

// TODO: What to do with the module?
EvosusLouPos;
```

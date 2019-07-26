/*

fileFolder plugin for jQuery.  Developed for the ProFoRMS project at NIH.

Dependencies:
	jQuery
	jQuery UI
	HashTable from http://www.mojavelinux.com/articles/javascript_hashes.html
	

json format for passing into tree:
{
	folders : [
		{
			name: "",
			folders : [{...}],
			files: [{...}],
			description: "",
			explanation: ""
		},
		...
	],
	files : [
		{
			id : "",
			name : "",
			description: "",
			author : "",
			type : "",
			url : "",
			pubMedId : "",
			fileName : ""
		},
		...
	]
}
 */
(function( $ ) {

	$.fn.fileFolder = function(method) {
		var settings = null;
		var $rootElement = null;
		var startingLocation = null;
		
		// --------------------------------------------------------------
		// Language Definitions
		
		/**
		 * U.S. English Language Definitions
		 */
		var oLanguage_en_US = {
			// Folder Error Messages
			rootFolderDeleteErrMsg : "You cannot delete the Regulatory E-Binder folder.",
			rootFolderEditErrMsg : "You cannot edit the Regulatory E-Binder folder.",
			folderNotFoundErrMsg : "Target folder could not be found.",
			folderNameRequriedErrMsg : "The folder name is required.",
			getFolderDuplicateErrMsg : function(folderName) {
				return "A folder named " + folderName + " already exists.";
			},
			getFolderNameMaxErrMsg : function(nameLimit) {
				return "The folder name cannot exceed " + nameLimit + " characters.";
			},
			
			// File Error Messages
			fileNotFoundErrMsg : "Target file could not be found.",
			fileNameRequiredMsg : "The File Name field is required.",
			fileDataRequiredMsg : "The uploaded file field is required.",
			getFileDuplicateErrMsg : function(fileName) {
				return "A file named " + fileName + " already exists.";
			},
			
			// Other Messages
			deletionErrMsg : "The selected file or folder could not be deleted because: ",
			fileSavingChangesMsg : "Saving changes...",
			fileUploadLimitMsg : "The file's size must be less than 250 MB.",
			deleteConfirmMsg : "Are you sure you want to delete the selected file or folder?",
			deleteFilesInFolderConfirmMsg : "Deleting this folder will also delete all files in it.  Are you sure you want to continue?",
			
			// Interface Element Labels
			rootFolderName : "Regulatory E-Binder",
			windowErrMsgTitle : "Error Message",
			windowFolderEditTitle : "Edit Folder",
			windowFileEditTitle : "Edit File",
			windowFileAddTitle : "Add File",
			folderNameLabel : "Name",
			folderDescLabel : "Description",
			folderExplainLabel : "Explanation",
			fileNameLabel : "Title",
			fileAuthorLabel : "Authors",
			fileDescLabel : "Description",
			fileTypeLabel : "Type",
			fileTypeOptions : {
				abstractOpt : "Abstract",
				poster : "Poster",
				manuscript : "Manuscript",
				other : "Other"
			},
			fileCurrUploadLabel : "Uploaded File",
			fileUploadInputLabel : "Upload File",
			fileUrlLabel : "URL",
			filePubMedLabel : "PubMed ID",
			uploadBtnTitle : "Upload",
			updateBtnTitle : "Update",
			cancelBtnTitle : "Cancel"
		};
		
		/**
		 * Chinese (Traditional) Language Definitions
		 */
		var oLanguage_zh_CN = {
			// Folder Error Messages
			rootFolderDeleteErrMsg : "您不能刪除監管E-賓德文件夾",
			rootFolderEditErrMsg : "你不能編輯監管的E-賓德文件夾",
			folderNotFoundErrMsg : "找不到目標文件夾",
			folderNameRequriedErrMsg : "需要文件夾名",
			getFolderDuplicateErrMsg : function(folderName) {
				return "已經存在一個文件夾，命名為" + folderName;
			},
			getFolderNameMaxErrMsg : function(nameLimit) {
				return "該文件夾名稱不能超過" + nameLimit + "字符";
			},
			
			// File Error Messages
			fileNotFoundErrMsg : "找不到目標文件",
			fileNameRequiredMsg : "“文件名”字段",
			fileDataRequiredMsg : "需要上傳的文件字段",
			getFileDuplicateErrMsg : function(fileName) {
				return "一個名為" + fileName + "已經存在";
			},
			
			// Other Messages
			deletionErrMsg : "選定的文件或文件夾無法刪除，因為: ",
			fileSavingChangesMsg : "保存更改...",
			fileUploadLimitMsg : "該文件的大小必須小於250 MB",
			deleteConfirmMsg : "你確定你要刪除選定的文件或文件夾？",
			deleteFilesInFolderConfirmMsg : "刪除此文件夾中的所有文件也將刪除。您確定要繼續嗎？",
			
			// Interface Element Labels
			rootFolderName : "監管E-賓德",
			windowErrMsgTitle : "錯誤訊息",
			windowFolderEditTitle : "編輯文件夾",
			windowFileEditTitle : "編輯文件",
			windowFileAddTitle : "添加文件",
			folderNameLabel : "稱號",
			folderDescLabel : "描述",
			folderExplainLabel : "解釋",
			fileNameLabel : "標題",
			fileAuthorLabel : "作者",
			fileDescLabel : "描述",
			fileTypeLabel : "類型",
			fileTypeOptions : {
				abstractOpt : "抽象",
				poster : "海報",
				manuscript : "手稿",
				other : "其他"
			},
			fileCurrUploadLabel : "上傳的文件",
			fileUploadInputLabel : "上傳文件",
			fileUrlLabel : "URL",
			filePubMedLabel : "PubMed ID",
			uploadBtnTitle : "上載",
			updateBtnTitle : "更新",
			cancelBtnTitle : "取消"
		};
		
		/**
		 * Chinese (Simplified) Language Definitions
		 */
		var oLanguage_zh_TW = {
			// Folder Error Messages
			rootFolderDeleteErrMsg : "你不能删除监管E-宾德文件夹的",
			rootFolderEditErrMsg : "你不能编辑监管的E-宾德文件夹",
			folderNotFoundErrMsg : "找不到目标文件夹",
			folderNameRequriedErrMsg : "需要文件夹名",
			getFolderDuplicateErrMsg : function(folderName) {
				return "名为" + folderName + "的文件夹已经存在";
			},
			getFolderNameMaxErrMsg : function(nameLimit) {
				return "文件夹名称不能超过" + nameLimit + "个字符";
			},
			
			// File Error Messages
			fileNotFoundErrMsg : "找不到目标文件",
			fileNameRequiredMsg : "\"文件名\"字段",
			fileDataRequiredMsg : "需要上传的文件字段",
			getFileDuplicateErrMsg : function(fileName) {
				return "名为" + fileName + "的文件已存在";
			},
			
			// Other Messages
			deletionErrMsg : "选定的文件或文件夹无法删除，因为: ",
			fileSavingChangesMsg : "保存更改...",
			fileUploadLimitMsg : "该文件的大小必须小于250 MB.",
			deleteConfirmMsg : "你确定你要删除选定的文件或文件夹?",
			deleteFilesInFolderConfirmMsg : "删除此文件夹中的所有文件也将删除.  你确定要继续?",
			
			// Interface Element Labels
			rootFolderName : "监管E-宾德",
			windowErrMsgTitle : "错误讯息",
			windowFolderEditTitle : "编辑文件夹",
			windowFileEditTitle : "编辑文件",
			windowFileAddTitle : "添加文件",
			folderNameLabel : "名",
			folderDescLabel : "描述",
			folderExplainLabel : "解释",
			fileNameLabel : "标题",
			fileAuthorLabel : "作者",
			fileDescLabel : "描述",
			fileTypeLabel : "类型",
			fileTypeOptions : {
				abstractOpt : "抽象",
				poster : "海报",
				manuscript : "手稿",
				other : "其他"
			},
			fileCurrUploadLabel : "上传的文件",
			fileUploadInputLabel : "上传文件",
			fileUrlLabel : "URL",
			filePubMedLabel : "PubMed ID",
			uploadBtnTitle : "上载",
			updateBtnTitle : "更新",
			cancelBtnTitle : "取消"
		};
		
		/**
		 * Korean Language Definitions
		 */
		var oLanguage_ko_KR = {
			// Folder Error Messages
			rootFolderDeleteErrMsg : "당신은 규제 E-바인더 폴더를 삭제할 수 없습니다",
			rootFolderEditErrMsg : "당신은 규제 E-바인더 폴더를 편집 할 수 없습니다",
			folderNotFoundErrMsg : "대상 폴더를 찾을 수 없습니다",
			folderNameRequriedErrMsg : "폴더 이름이 필요합니다",
			getFolderDuplicateErrMsg : function(folderName) {
				return folderName + " 라는 이름의 폴더가 이미 존재합니다";
			},
			getFolderNameMaxErrMsg : function(nameLimit) {
				return "폴더 이름은 " + nameLimit + " 문자를 초과 할 수 없습니다";
			},
			
			// File Error Messages
			fileNotFoundErrMsg : "대상 파일을 찾을 수 없습니다",
			fileNameRequiredMsg : "파일 이름 필드가 필요합니다",
			fileDataRequiredMsg : "업로드 파일 필드가 필요합니다",
			getFileDuplicateErrMsg : function(fileName) {
				return "이미 " + fileName + " 존재라는 이름의 파일";
			},
			
			// Other Messages
			deletionErrMsg : "선택한 파일이나 폴더가 삭제되지 않을 수 있기 때문에: ",
			fileSavingChangesMsg : "변경 사항 저장...",
			fileUploadLimitMsg : "파일의 크기는 250 미만 MB의합니다",
			deleteConfirmMsg : "당신은 당신이 선택한 파일 또는 폴더를 삭제 하시겠습니까?",
			deleteFilesInFolderConfirmMsg : "이 폴더를 삭제하면 그 안의 모든 파일을 삭제합니다. 당신은 당신이 계속 하시겠습니까?",
			
			// Interface Element Labels
			rootFolderName : "규제 E-바인더",
			windowErrMsgTitle : "오류 메시지",
			windowFolderEditTitle : "폴더 편집",
			windowFileEditTitle : "파일을 편집",
			windowFileAddTitle : "파일 추가",
			folderNameLabel : "이름",
			folderDescLabel : "기술",
			folderExplainLabel : "설명",
			fileNameLabel : "표제",
			fileAuthorLabel : "저자",
			fileDescLabel : "기술",
			fileTypeLabel : "유형",
			fileTypeOptions : {
				abstractOpt : "추상",
				poster : "포스터",
				manuscript : "원고",
				other : "다른"
			},
			fileCurrUploadLabel : "업로드 파일",
			fileUploadInputLabel : "파일 업로드",
			fileUrlLabel : "URL",
			filePubMedLabel : "PubMed ID",
			uploadBtnTitle : "업로드",
			updateBtnTitle : "새롭게 하다",
			cancelBtnTitle : "취소"
		};
		
		/**
		 * Spanish Language Definitions
		 */
		var oLanguage_es = {
			// Folder Error Messages
			rootFolderDeleteErrMsg : "No se puede eliminar la carpeta E-Binder Reguladora.",
			rootFolderEditErrMsg : "No se puede editar la carpeta E-Binder Reguladora.",
			folderNotFoundErrMsg : "Carpeta de destino no se pudo encontrar.",
			folderNameRequriedErrMsg : "Se requiere el nombre de la carpeta.",
			getFolderDuplicateErrMsg : function(folderName) {
				return "Una carpeta llamada " + folderName + " ya existe.";
			},
			getFolderNameMaxErrMsg : function(nameLimit) {
				return "El nombre de la carpeta no puede exceder de " + nameLimit + " caracteres.";
			},
			
			// File Error Messages
			fileNotFoundErrMsg : "El archivo de destino no se pudo encontrar.",
			fileNameRequiredMsg : "El campo Nombre de archivo es necesaria.",
			fileDataRequiredMsg : "El campo archivo subido se requiere.",
			getFileDuplicateErrMsg : function(fileName) {
				return "Un archivo con el nombre " + fileName + " ya existe.";
			},
			
			// Other Messages
			deletionErrMsg : "El archivo o carpeta seleccionado no se pueden eliminar porque: ",
			fileSavingChangesMsg : "Cómo guardar los cambios...",
			fileUploadLimitMsg : "El tamaño del archivo debe ser inferior a 250 MB.",
			deleteConfirmMsg : "¿Está seguro que desea eliminar el archivo o carpeta seleccionado?",
			deleteFilesInFolderConfirmMsg : "Eliminar esta carpeta también se eliminarán todos los archivos que contiene. ¿Está seguro que desea continuar?",
			
			// Interface Element Labels
			rootFolderName : "Reguladora E-Binder",
			windowErrMsgTitle : "Mensaje de Error",
			windowFolderEditTitle : "Editar Carpeta",
			windowFileEditTitle : "Editar Archivo",
			windowFileAddTitle : "Agregar Archivo",
			folderNameLabel : "Nombre",
			folderDescLabel : "Descripción",
			folderExplainLabel : "Explicación",
			fileNameLabel : "Título",
			fileAuthorLabel : "Autores",
			fileDescLabel : "Descripción",
			fileTypeLabel : "Tipo",
			fileTypeOptions : {
				abstractOpt : "Abstracto",
				poster : "Cartel",
				manuscript : "Manuscrito",
				other : "Otro"
			},
			fileCurrUploadLabel : "Archivo Subido",
			fileUploadInputLabel : "Subir Archivo",
			fileUrlLabel : "URL",
			filePubMedLabel : "PubMed ID",
			uploadBtnTitle : "Subir",
			updateBtnTitle : "Actualizar",
			cancelBtnTitle : "Cancelar"
		};
		
		/* Utility and Object Classes ******************************* */
		
		/**
		 * Represents a folder as an object in memory so we can operate on it
		 * and then retrieve this list for saving back to the database
		 */
		function Folder(config) {
			this.name = "";
			this.folders = new HashTable();
			this.files = new HashTable();
			this.description = "";
			this.explanation = "";
			
			if (config) {
				this.loadConfig(config);
			}
		}
			Folder.prototype.loadConfig = function(config) {
				if (typeof config.name !== "undefined") {
					this.name = config.name;
				}
				if (typeof config.description !== "undefined") {
					this.description = config.description;
				}
				if (typeof config.explanation !== "undefined") {
					this.explanation = config.explanation;
				}
				if (typeof config.folders !== "undefined") {
					for (var i = 0; i < config.folders.length; i++) {
						this.folders.setItem(config.folders[i].name, new Folder(config.folders[i]));
					}
				}
				if (typeof config.files !== "undefined") {
					for (var j = 0; j < config.files.length; j++) {
						this.files.setItem(config.files[j].name, new File(config.files[j]));
					}
				}
			};
			
			/**
			 * Adds a folder under this folder.  This operate similarly to an OO
			 * recursive method.
			 * 
			 * If the location parameter is not given, the parent folder is
			 * assumed to be the current folder
			 * 
			 * @param folder - the Folder to add
			 * @param location - (opt) the url location of the new folder's parent folder
			 * @param settings - the settings object for the messages
			 * @returns folder if added, null if a folder by that name already exists
			 * @throws error on target directory not found
			 */
			Folder.prototype.addFolder = function(folder, location, settings) {
				if (typeof location !== "undefined" && location !== "" && location !== this.name) {
					// if the location is below this folder, enter the correct
					// child folder, and let it propagate down until found
					var locArr = location.split("/");
					if (this.folders.hasItem(locArr[0])) {
						var childFolder = this.folders.getItem(locArr[0]);
						locArr.shift();
						return childFolder.addFolder(folder, locArr.join("/"), settings);
					}
					else {
						throw new Error(settings.oLanguage.folderNotFoundErrMsg);
					}
				}
				else {
					if (this.folders.getItem(folder.name) !== undefined && this.files.getItem(folder.name) !== undefined) {
						throw new Error(settings.oLanguage.getFolderDuplicateErrMsg(folder.name));
					}
					else {
						this.folders.setItem(folder.name, folder);
						return folder;
					}
				}
			};
			
			/**
			 * Adds a file under this folder
			 * 
			 * @param file - the File to add
			 * @param location - the location of the file's parent folder
			 * @param settings - the settings object
			 * @returns file if added, null if a file by that name already exists
			 */
			Folder.prototype.addFile = function(file, location, settings) {
				// are we trying to operate inside this folder?
				if (typeof location !== "undefined" && location !== "" && location !== this.name) {
					// nope
					var locArr = location.split("/");
					if (this.folders.hasItem(locArr[0])) {
						var childFolder = this.folders.getItem(locArr[0]);
						locArr.shift();
						return childFolder.addFile(file, locArr.join("/"), settings);
					}
					else {
						throw new Error(settings.oLanguage.folderNotFoundErrMsg);
					}
				}
				else {
					// yes, we are operating inside this folder
					if (this.files.getItem(file.name) !== undefined) {
						throw new Error(settings.oLanguage.getFileDuplicateErrMsg(file.name));
					}
					else {
						this.files.setItem(file.name, file);
						return file;
					}
				}
			};
			
			/**
			 * Removes a folder from the tree below this folder
			 * 
			 * @param name - the name of the folder to remove
			 * @param settings - the settings object
			 * @returns the folder that was removed if removed
			 * @throws Error on error
			 */
			Folder.prototype.removeFolder = function(name, location, settings) {
				// is the folder inside this one?
				if (typeof location !== "undefined" && location.indexOf("/") !== -1) {
					// no, it's not.  Propagate down the tree following the location string
					var locArr = location.split("/");
					if (this.folders.hasItem(locArr[0])) {
						var childFolder = this.folders.getItem(locArr[0]);
						locArr.shift();
						return childFolder.removeFolder(name, locArr.join("/"), settings);
					}
					else {
						throw new Error(settings.oLanguage.folderNotFoundErrMsg);
					}
				}
				else {
					if (this.folders.getItem(name) === undefined) {
						throw new Error (settings.oLanguage.folderNotFoundErrMsg);
					}
					else {
						var previous = this.folders.removeItem(name);
						return previous;
					}
				}
			};
			
			/**
			 * Removes a file from under this one
			 * 
			 * @param name the name of the file to remove
			 * @param settings - the settings object
			 * @returns the file that was removed if removed, otherwise null
			 */
			Folder.prototype.removeFile = function(name, location, settings) {
				// is the file inside this one?
				if (typeof location !== "undefined" && location.indexOf("/") !== -1) {
					// no, it's not.  Propagate down the tree following the location string
					var locArr = location.split("/");
					if (this.folders.hasItem(locArr[0])) {
						var childFolder = this.folders.getItem(locArr[0]);
						locArr.shift();
						return childFolder.removeFile(name, locArr.join("/"), settings);
					}
					else {
						throw new Error(settings.oLanguage.folderNotFoundErrMsg);
					}
				}
				else {
					if (this.files.getItem(name) === undefined) {
						throw new Error (settings.oLanguage.fileNotFoundErrMsg);
					}
					else {
						var previous = this.files.removeItem(name);
						return previous;
					}
				}
			};
			
			/**
			 * Modifies a folder stored in the data tree
			 * 
			 * @param newFolder the new folder holding the new meta data to store
			 * @param location the original location of the folder
			 * @returns the new folder
			 * @throws error on invalid location
			 */
			Folder.prototype.updateFolder = function(newFolder, location, settings) {
				if (typeof location !== "undefined" && location.indexOf("/") !== -1) {
					// if the location is below this folder, enter the correct
					// child folder, and let it propagate down until found
					var locArr = location.split("/");
					if (this.folders.hasItem(locArr[0])) {
						var childFolder = this.folders.getItem(locArr[0]);
						locArr.shift();
						return childFolder.updateFolder(newFolder, locArr.join("/"), settings);
					}
					else {
						throw new Error(settings.oLanguage.folderNotFoundErrMsg);
					}
				}
				else {
					var element = this.folders.getItem(location);
					
					// Check if the element was retrieved
					if (element === undefined) {
						throw new Error (settings.oLanguage.folderNotFoundErrMsg);
					}
					
					newFolder.folders = element.folders;
					newFolder.files = element.files;
					
					if (element.name !== newFolder.name) {
						this.folders.removeItem(location);
						this.folders.setItem(newFolder.name, newFolder);
					}
					else {
						this.folders.setItem(element.name, newFolder);
					}
					return newFolder;
				}
			};
			
			/**
			 * Modifies a file stored in the data tree
			 * 
			 * @param newFile - the new file to store
			 * @param location - the original location of the file
			 * @param settings - the settings object
			 * @returns the new file
			 * @throws error on invalid location
			 */
			Folder.prototype.updateFile = function(newFile, location, settings) {
				if (typeof location !== "undefined" && location.indexOf("/") !== -1) {
					// if the location is below this folder, enter the correct
					// child folder, and let it propagate down until found
					var locArr = location.split("/");
					if (this.folders.hasItem(locArr[0])) {
						var childFolder = this.folders.getItem(locArr[0]);
						locArr.shift();
						return childFolder.updateFile(newFile, locArr.join("/"), settings);
					}
					else {
						throw new Error(settings.oLanguage.folderNotFoundErrMsg);
					}
				}
				else {
					var element = this.files.getItem(location);
					if (element === undefined) {
						throw new Error (settings.oLanguage.fileNotFoundErrMsg);
					}
					if (element.name !== newFile.name) {
						this.files.removeItem(location);
						this.files.setItem(newFile.name, newFile);
					}
					else {
						this.files.setItem(element.name, newFile);
					}
					return newFile;
				}
			};
			
			Folder.prototype.getFolder = function(location, settings) {
				if (typeof location !== "undefined" && location.indexOf("/") !== -1) {
					// if the location is below this folder, enter the correct
					// child folder, and let it propagate down until found
					var locArr = location.split("/");
					if (this.folders.hasItem(locArr[0])) {
						var childFolder = this.folders.getItem(locArr[0]);
						locArr.shift();
						return childFolder.getFolder(locArr.join("/"), settings);
					}
					else {
						throw new Error(settings.oLanguage.folderNotFoundErrMsg);
					}
				}
				else {
					var element = this.folders.getItem(location);
					if (element === undefined) {
						throw new Error(settings.oLanguage.folderNotFoundErrMsg);
					}
					return element;
				}
			};
			
			Folder.prototype.getFile = function(location, settings) {
				if (typeof location !== "undefined" && location.indexOf("/") !== -1) {
					// if the location is below this folder, enter the correct
					// child folder, and let it propagate down until found
					var locArr = location.split("/");
					if (this.folders.hasItem(locArr[0])) {
						var childFolder = this.folders.getItem(locArr[0]);
						locArr.shift();
						return childFolder.getFile(locArr.join("/"), settings);
					}
					else {
						throw new Error(settings.oLanguage.folderNotFoundErrMsg);
					}
				}
				else {
					var element = this.files.getItem(location);
					if (element === undefined) {
						throw new Error(settings.oLanguage.fileNotFoundErrMsg);
					}
					return element;
				}
			};
		
		
		function File(config) {
			this.id = "-1";
			this.name = "";
			this.description = "";
			this.author = "";
			this.type = "";
			this.url = "";
			this.pubMedId = "";
			this.fileName = "";
			
			if (config) {
				this.loadConfig(config);
			}
		}
		File.prototype.loadConfig = function(config) {
			for (var k in config) {
				if (config.hasOwnProperty(k)) {
					this[k] = config[k];
				}
			}
		};
		
		/* End Utility and Object Classes ******************************* */
		
		/**
		 * Handles the switching between input field and regular text for
		 * operations such as "rename" and "create folder"
		 */
		var EditFieldController = {
			suppressBlur : false,
			
			/**
			 * Converts the input element $inputElement into a standard text
			 * representation of the string within the input field			
			 * 
			 * @param $inputElement jquery reference of the input element
			 */
			convertToString : function($inputElement) {
				var text = $inputElement.val().trim();
				var $parent = $inputElement.parent();
				$inputElement.remove();
				$parent.text(text);
				return text;
			},
			
			/**
			 * Converts the text inside the given span into an input field
			 * so it can be edited.
			 * 
			 * Accepts a changeCallback function that accepts a single
			 * parameter: string text
			 * 
			 * @param $spanToConvert the span to convert
			 */
			convertToInput : function($spanToConvert, settings, changeCallback) {
				var text = $spanToConvert.text();
				$spanToConvert.text("");
				var $input = $spanToConvert.append(this.inputHtml).find("input");
				$input.val(text);
				$input.focus().click();
				$input.keydown(function(e) {
					if ( e.which === 13 ) {
						EditFieldController.suppressBlur = true;
						e.preventDefault();
						
						if ( typeof changeCallback !== "undefined" ) {
							try {
								changeCallback(EditFieldController.getText($(this)));
								EditFieldController.convertToString($(this));
							}
							catch(exception) {
								$.ibisMessaging("dialog", "error", exception.message, {title: settings.oLanguage.windowErrMsgTitle});
							}
						}
						
						EditFieldController.suppressBlur = false;
					}
					else if ( e.which === 27 ) {
						// The escape key was pressed.  Remove the new folder from the DOM.
						e.preventDefault();
						$(this).parent().parent().remove();
					}
				});
				$input.blur(function(event) {
					if ( !EditFieldController.suppressBlur ) {
						event.preventDefault();
						
						if ( typeof changeCallback !== "undefined" ) {
							try {
								changeCallback(EditFieldController.getText($(this)));
								EditFieldController.convertToString($(this));
							}
							catch(exception) {
								$.ibisMessaging("dialog", "error", exception.message, {title: settings.oLanguage.windowErrMsgTitle});
							}
						}
					}
				});
			},
			
			getText : function($span) {
				return $span.val();
			},
			
			inputHtml : '<input type="text" class="fileFolder-editField" value="new folder" />'
		};
		
		var EditElementUtil = {
			/**
			 * This is put here because it needs to be global enough to exist
			 * outside the event calls later on
			 */
			uploadFile : new File(),
			editFileFormTitle : "Add/Edit File",
			init : function() {
				// add the iframe to the page
				if ($("#filefolder-uploadtarget").length < 1) {
					$("body").append('<iframe id="filefolder-uploadtarget" name="filefolder-uploadtarget" src="_blank" style="width:0;height:0;border:0px solid #fff;">iframe</iframe>');
				}
			},
			
			goodMessage : function(message) {
				$.ibisMessaging("primary", "success", message, {container: ".fileFolder-editFolder-messages"});
			},
			
			badMessage : function(message) {
				$.ibisMessaging("primary", "error", message, {container: ".fileFolder-editFolder-messages"});
			},
			
			clearMessage : function() {
				$(".fileFolder-editFolder-messages").empty();
			},
			
			editFile : function(currentFile, settings, callback) {
				// Check if the file edit form is already created.
				// If it is, remove it from the DOM
				if ( $("#fileFolder-editFile").length > 0 ) {
					$("#fileFolder-editFile").dialog("destroy").remove();
				}
				
				// Create the file edit form
				$("body").append(this.editFileWindow(currentFile, settings));
				convertFileInputs();
				$('#fileFolder-updatebutton').click(function() {
					EditElementUtil.clearMessage();
					// validate:
					if ($("#fileFolder-fileName").val() === "") {
						EditElementUtil.badMessage(settings.oLanguage.fileNameRequiredMsg);
					}
					else if (($("#fileFolder-uploadedDisplayFileName").length == 0) && ($("#fileFolder-fileFile").val() === "")) {
						EditElementUtil.badMessage(settings.oLanguage.fileDataRequiredMsg);
					}
					else {
						// prep for upload
						$("#filefolder-uploadtarget").off("load");
						$("#filefolder-uploadtarget").on("load", function(event) {
							// the upload has completed
							var $iframeContents = $("#filefolder-uploadtarget").contents();
							EditElementUtil.uploadFile = new File();
							EditElementUtil.uploadFile.id = $iframeContents.find("#fileFolder-fileId").val();
							EditElementUtil.uploadFile.name = $iframeContents.find("#fileFolder-fileName").val();
							EditElementUtil.uploadFile.author = $iframeContents.find("#fileFolder-fileAuthor").val();
							EditElementUtil.uploadFile.description = $iframeContents.find("#fileFolder-fileDesc").val();
							EditElementUtil.uploadFile.type = $iframeContents.find("#fileFolder-fileType").val();
							EditElementUtil.uploadFile.url = $iframeContents.find("#fileFolder-fileUrl").val();
							EditElementUtil.uploadFile.pubMedId = $iframeContents.find("#fileFolder-filePubMedId").val();
							EditElementUtil.uploadFile.fileName = $iframeContents.find("#fileFolder-uploadedFileName").val();
							
							if ( $iframeContents.find("#messageContainer").find(".ibisMessaging-error").length == 0 ) {
								settings.onFileUpload(EditElementUtil.uploadFile, true);
								$("#fileFolder-fileName").val("");
								$("#fileFolder-fileAuthor").val("");
								$("#fileFolder-fileDesc").val("");
								$("#fileFolder-fileType option:selected").prop("selected", false);
								$("#fileFolder-fileFile").val("");
								$("#fileFolder-filePubMedId").val("");
								$("#fileFolder-uploadedDisplayFileName").empty();
								$("#fileFolder-editFile").dialog("close");
								callback(EditElementUtil.uploadFile, true);
							}
							else {
								// displays the error that doesn't close the dialog box
								EditElementUtil.clearMessage();
								$iframeContents.find("#messageContainer").find(".ibisMessaging-error").each(function() {
									EditElementUtil.badMessage($(this).text());
								});
								settings.onFileUpload(EditElementUtil.uploadFile, false);
							}
						});
						$("#filefolder-editfileform").submit();
						EditElementUtil.goodMessage(settings.oLanguage.fileSavingChangesMsg);
					}
				});
				
				$("#fileFolder-editFile").dialog({
					draggable: false,
					hide: "fade",
					modal: true,
					show: "fade",
					width: 535,
					title: EditElementUtil.editFileFormTitle
				});
			},
			editFolder : function(currentFolder, settings, callback) {
				// Check if the edit folder was already created.
				// If so, remove it from the DOM
				if ( $("#fileFolder-editFolder").length > 0 ) {
					$("#fileFolder-editFolder").dialog("destroy").remove();
				}
				
				// Create the edit folder form
				$("body").append(this.editFolderWindow(currentFolder, settings));
				$('#fileFolder-updatebutton').click(function() {
					EditElementUtil.clearMessage();
					
					// Check for the folder name
					if ( $("#fileFolder-folderName").val() === "" ) {
						EditElementUtil.badMessage(settings.oLanguage.folderNameRequriedErrMsg);
						return;
					}
					else if ( $("#fileFolder-folderName").val().length > 50 ) {
						EditElementUtil.badMessage(settings.oLanguage.getFolderNameMaxErrMsg(50));
						return;
					}
					
					var folder = new Folder();
					folder.name = $("#fileFolder-folderName").val();
					folder.description = $("#fileFolder-folderDesc").val();
					folder.explanation = $("#fileFolder-folderExpl").val();
					$("#fileFolder-folderName").val("");
					$("#fileFolder-folderDesc").val("");
					$("#fileFolder-folderExpl").val("");
					$("#fileFolder-editFolder").dialog("close");
					callback(folder);
				});
				
				$("#fileFolder-editFolder").dialog({
					draggable: false,
					hide: "fade",
					modal: true,
					show: "fade",
					width: 535,
					title: settings.oLanguage.windowFolderEditTitle
				});
			},
			editFolderWindow : function(currentFolder, settings) {
				var output = '<div id="fileFolder-editFolder" style="display: none;"> \
				<div class="fileFolder-editFolder-messages"></div><form> \
				<div class="fileFolder-inputRow"> \
				<div class="fileFolder-inputLabel">'+settings.oLanguage.folderNameLabel+'<span class="requiredStar">*</span></div> \
				<div class="fileFolder-input"><input type="text" id="fileFolder-folderName" value="'+currentFolder.name+'" /></div> \
				<div style="clear: both"></div> \
				</div> \
				<div class="fileFolder-inputRow"> \
				<div class="fileFolder-inputLabel">'+settings.oLanguage.folderDescLabel+'</div> \
				<div class="fileFolder-input"><textarea id="fileFolder-folderDesc">'+currentFolder.description+'</textarea></div> \
				<div style="clear: both"></div> \
				</div> \
				<div class="fileFolder-inputRow"> \
				<div class="fileFolder-inputLabel">'+settings.oLanguage.folderExplainLabel+'</div> \
				<div class="fileFolder-input"><textarea id="fileFolder-folderExpl">'+currentFolder.explanation+'</textarea></div> \
				<div style="clear: both"></div> \
				</div> \
				<div class="fileFolder-buttonContainer"><input type="button" value="'+settings.oLanguage.updateBtnTitle+'" id="fileFolder-updatebutton" />\
				<input type="button" value="'+settings.oLanguage.cancelBtnTitle+'" onclick="$(\'#fileFolder-editFolder\').dialog(\'close\').dialog(\'destroy\').remove();" /></div> \
				</form></div>';
				return output;
			},
			editFileWindow : function(currentFile, settings) {
				var output = '<div id="fileFolder-editFile"><div class="fileFolder-editFolder-messages"></div>\
				<form name="filefolder-editfileform" id="filefolder-editfileform" target="filefolder-uploadtarget" action="'+settings.fileUpload.targetUrl+'" method="post" enctype="multipart/form-data"> \
				<input type="hidden" name="action" value="process_upload" /> \
				<input type="hidden" name="'+settings.fileUpload.fieldNames.id+'" id="fileFolder-fileId" value="'+currentFile.id+'" /> \
				<input type="hidden" name="'+settings.fileUpload.fieldNames.fileName+'" id="fileFolder-uploadedFileName" value="'+currentFile.fileName+'" /> \
				<div class="fileFolder-inputRow"> \
				<div class="fileFolder-inputLabel">'+settings.oLanguage.fileNameLabel+'<span class="requiredStar">*</span></div> \
				<div class="fileFolder-input"><input type="text" name="'+settings.fileUpload.fieldNames.name+'" id="fileFolder-fileName" value="'+currentFile.name+'" /></div> \
				<div style="clear: both"></div> \
				</div> \
				<div class="fileFolder-inputRow"> \
				<div class="fileFolder-inputLabel">'+settings.oLanguage.fileAuthorLabel+'</div> \
				<div class="fileFolder-input"><input type="text" name="'+settings.fileUpload.fieldNames.author+'" id="fileFolder-fileAuthor" value="'+currentFile.author+'" /></div> \
				<div style="clear: both"></div> \
				</div> \
				<div class="fileFolder-inputRow"> \
				<div class="fileFolder-inputLabel">'+settings.oLanguage.fileDescLabel+'</div> \
				<div class="fileFolder-input"><textarea name="'+settings.fileUpload.fieldNames.description+'" id="fileFolder-fileDesc">'+currentFile.description+'</textarea></div> \
				<div style="clear: both"></div> \
				</div> \
				<div class="fileFolder-inputRow"> \
				<div class="fileFolder-inputLabel">'+settings.oLanguage.fileTypeLabel+'</div> \
				<div class="fileFolder-input"><select name="'+settings.fileUpload.fieldNames.type+'" id="fileFolder-fileType">';
				if ( currentFile.type == '10' ) {
					output += '<option value="10" selected="selected">'+settings.oLanguage.fileTypeOptions.abstractOpt+'</option>';
				} else {
					output += '<option value="10">'+settings.oLanguage.fileTypeOptions.abstractOpt+'</option>';
				}
				if ( currentFile.type == '12' ) {
					output += '<option value="12" selected="selected">'+settings.oLanguage.fileTypeOptions.poster+'</option>';
				} else {
					output += '<option value="12">'+settings.oLanguage.fileTypeOptions.poster+'</option>';
				}
				if ( currentFile.type == '14' ) {
					output += '<option value="14" selected="selected">'+settings.oLanguage.fileTypeOptions.manuscript+'</option>';
				} else {
					output += '<option value="14">'+settings.oLanguage.fileTypeOptions.manuscript+'</option>';
				}
				if ( currentFile.type == '16' ) {
					output += '<option value="16" selected="selected">'+settings.oLanguage.fileTypeOptions.other+'</option>';
				} else {
					output += '<option value="16">'+settings.oLanguage.fileTypeOptions.other+'</option>';
				}
				output += '</select></div> \
				<div style="clear: both"></div> \
				</div>';
				if ( currentFile.fileName !== "" ) {
					output += '<div class="fileFolder-inputRow"> \
					<div class="fileFolder-inputLabel">'+settings.oLanguage.fileCurrUploadLabel+'</div> \
					<div class="fileFolder-input"><span id="fileFolder-uploadedDisplayFileName">'+currentFile.fileName+'</span></div> \
					<div style="clear: both"></div> \
					</div>';
				}
				output += '<div class="fileFolder-inputRow"> \
				<div class="fileFolder-inputLabel">'+settings.oLanguage.fileUploadInputLabel+'<span class="requiredStar">*</span></div> \
				<div class="fileFolder-input"><input type="file" name="'+settings.fileUpload.fieldNames.file+'" id="fileFolder-fileFile" /></div> \
				<input type="submit" style="display: none" /> \
				<div style="clear: both"></div> \
				</div> \
				<div class="fileFolder-inputRow"> \
				<div class="fileFolder-inputLabel">&nbsp;</div> \
				<div class="fileFolder-input"><span>'+settings.oLanguage.fileUploadLimitMsg+'</span></div> \
				<div style="clear: both"></div> \
				</div> \
				<div class="fileFolder-inputRow"> \
				<div class="fileFolder-inputLabel">'+settings.oLanguage.fileUrlLabel+'</div> \
				<div class="fileFolder-input"><input type="text" name="'+settings.fileUpload.fieldNames.url+'" id="fileFolder-fileUrl" value="'+currentFile.url+'" /></div> \
				<div style="clear: both"></div> \
				</div> \
				<div class="fileFolder-inputRow"> \
				<div class="fileFolder-inputLabel">'+settings.oLanguage.filePubMedLabel+'</div> \
				<div class="fileFolder-input"><input type="text" name="'+settings.fileUpload.fieldNames.pubMedId+'" id="fileFolder-filePubMedId" value="'+currentFile.pubMedId+'" /></div> \
				<div style="clear: both"></div> \
				</div> \
				<div class="fileFolder-buttonContainer"><input type="button" value="';
				if (currentFile.name === "") {
					output += settings.oLanguage.uploadBtnTitle;
				}
				else {
					output += settings.oLanguage.updateBtnTitle;
				}
				output += '" id="fileFolder-updatebutton" />\
				<input type="button" value="'+settings.oLanguage.cancelBtnTitle+'" onclick="$(\'#fileFolder-editFile\').dialog(\'close\').dialog(\'destroy\').remove();" /></div> \
				</form></div>';
				return output;
			}
		};
		
		// public functions go here
		var methods = {
			init : function(options) {
				settings = $.extend({}, $.fn.fileFolder.defaults, options);
				settings.oLanguage = determineLanguage();
				
				EditElementUtil.init();
				// initialize on each matching element
				// $this is the element being called as in
				// $(element).fileFolder("...");
				return this.each(function() {
					var $this = $(this);
					settings.$rootElement = $this;
					setSettings($this, settings);
					initTree($this);
				});
			},
			
			addFolder : function($rootElement) {
				var $selected = getSelected($rootElement);
				var settings = getSettings($rootElement);
				
				if ($selected.children("ul").length < 1) {
					// in this case, $selected is a file so get its parent folder
					$selected = $selected.parents("li");
				}
				
				openFolder($rootElement, $selected);
				$selected.children("ul").append('<li><span></span><ul></ul></li>');
				var $parentFolder = $selected.children("ul").children("li").last();
				// initialize the interactions for opening the "new folder" input box
				EditFieldController.convertToInput($parentFolder.children("span"), settings, function(text) {
					// Validate the folder name
					if ( text === "" ) {
						throw new Error(settings.oLanguage.folderNameRequriedErrMsg);
					}
					else if ( text.length > 50 ) {
						throw new Error(settings.oLanguage.getFolderNameMaxErrMsg(50));
					}
					
					closeFolder($rootElement, $parentFolder);
					var folder = new Folder({
						name : text,
						folders : [],
						files : []
					});
					
					try {
						var tree = getTree($rootElement);
		
						destroyDragDrop($rootElement);
						
						// add the folder
						tree.addFolder(folder, getNodeLocation($parentFolder.parents("li").eq(0)), settings);
						EditFieldController.convertToString($parentFolder.find("input"));
						
						refreshTreeClasses($rootElement);
						initDragDrop($rootElement);
						settings.onAddFolder(folder);
					}
					catch(e) {
						// bubble up the error so the text box doesn't close
						throw new Error(e.message);
					}
				});
			},
			
			deleteSelected : function($rootElement) {
				var $selected = getSelected($rootElement);
				var location = getNodeLocation($selected);
				var tree = getTree($rootElement);
				var settings = getSettings($rootElement);
				
				if ($selected.hasClass("fileFolder-rootLi")) {
					$.ibisMessaging("dialog", "error", settings.oLanguage.rootFolderDeleteErrMsg, {title: settings.oLanguage.windowErrMsgTitle});
					return;
				}
				
				try {
					if (!settings.verifyDelete || confirm(settings.oLanguage.deleteConfirmMsg)) {
						// make sure to use this function instead of coupling todelete and selected
						var $toDelete = getSelected($rootElement);
						if (!isFolder($selected)) {
							var file = tree.removeFile($selected.children("span").text(), location, settings);
							// select an element nearest to this
							if ($selected.next().length > 0) {
								select($selected.next());
							}
							else if ($selected.prev().length > 0) {
								select($selected.prev());
							}
							else {
								select($selected.parent());
							}
							
							$toDelete.remove();
							getSettings($rootElement).onDeleteFile(file);
						}
						else {
							// deleting folders is much more complicated
							var confirmGo = true;
							if ($selected.find("li").length > 0 && !confirm(settings.oLanguage.deleteFilesInFolderConfirmMsg)) {
								confirmGo = false;
							}
							
							if (confirmGo) {
								var folder = tree.removeFolder($selected.children("span").text(), location, settings);
								// select an element nearest to this 
								if ($selected.next().length > 0) {
									select($selected.next());
								}
								else if ($selected.prev().length > 0) {
									select($selected.prev());
								}
								else {
									select($selected.parent());
								}
								
								$toDelete.remove();
								getSettings($rootElement).onDeleteFolder(folder);
							}
						}
					}
				}
				catch(exception) {
					throw new Error(settings.oLanguage.deletionErrMsg + exception.message);
				}
			},
			
			editSelected : function($rootElement) {
				var $selected = getSelected($rootElement);
				var location = getNodeLocation($selected);
				var tree = getTree($rootElement);
				var settings = getSettings($rootElement);
				
				if ($selected.hasClass("fileFolder-rootLi")) {
					$.ibisMessaging("dialog", "error", settings.oLanguage.rootFolderEditErrMsg, {title: settings.oLanguage.windowErrMsgTitle});
					return;
				}
				
				if (isFolder($selected)) {
					// it's a folder
					var folder = tree.getFolder(location, settings);
					EditElementUtil.editFolder(folder, settings, function(newFolder) {
						var $updated = tree.updateFolder(newFolder, location, settings);
						$selected.children("span").text(newFolder.name);
						$("#fileFolder-editFolder").dialog("destroy").remove();
						settings.onEdit($selected, $updated);
					});
				}
				else {
					// it's a file
					var file = tree.getFile(location, settings);
					EditElementUtil.editFileFormTitle = settings.oLanguage.windowFileEditTitle;
					EditElementUtil.editFile(file, settings, function(newFile, uploadSuccess) {
						var $updated = tree.updateFile(newFile, location, settings);
						$selected.children("span").text(newFile.name);
						$("#fileFolder-editFile").dialog("destroy").remove();
						settings.onEdit($selected, $updated);
					});
				}
			},
			
			addFile : function($rootElement) {
				var $selected = getSelected($rootElement);
				if ($selected.children("ul").length < 1) {
					// in this case, $selected is a file so get its parent folder
					$selected = $selected.parents("li");
				}
				var location = getNodeLocation($selected);
				var tree = getTree($rootElement);
				var settings = getSettings($rootElement);
				
				var file = new File();
				EditElementUtil.editFileFormTitle = settings.oLanguage.windowFileAddTitle;
				EditElementUtil.editFile(file, settings, function(newFile, uploadSuccess) {
					if (uploadSuccess) {
						destroyDragDrop($rootElement);
						
						$selected.children("ul").append('<li><span>'+newFile.name+'</span></li>');
						tree.addFile(newFile, location, settings);
						
						refreshTreeClasses($rootElement);
						initDragDrop($rootElement);
						
						$("#fileFolder-editFile").dialog("destroy").remove();
						settings.onAddFile($selected.children("ul").children("li").last());
					}
				});
			},
			
			downloadSelected : function($rootElement) {
				var $selected = getSelected($rootElement);
				var location = getNodeLocation($selected);
				var tree = getTree($rootElement);
				var settings = getSettings($rootElement);
				var fileArray = new Array();
				
				if ( isFolder($selected) ) {
					var folder = tree.getFolder(location, settings);
					fileArray = folder.files.values();
				}
				else {
					fileArray.push(tree.getFile(location, settings));
				}
				
				settings.onDownload(fileArray);
			},
			
			getTreeAsJSON : function($rootElement) {
				var inTree = getTree($rootElement);
				var outTree = new Object();
				
				// Set the root elements
				outTree.folders = convertBranch(inTree.folders.values());
				outTree.files = inTree.files.values();
				
				return outTree;
			}
		};
		
		// ----------------------------------------------------------------
		// private functions go here
		
		/**
		 * Takes in an array of Folder objects and converts them into a branch of fileFolder objects at will
		 * be stored in the database. Since the Folder object's "folders" member variable can hold a HashTable
		 * of Folder objects, recursive calls to this function will be made to convert the rest of the Folder objects.
		 * 
		 * @param branch - An array of Folder objects representing a branch of the fileFolder tree.
		 * @returns	The converted array of files and folders to be stored in the database.
		 */
		function convertBranch(branch) {
			var folderArray = [];
			var folder = null;
			
			// Loop through the Folder objects
			for ( var i = 0; i < branch.length; i++ ) {
				folder = new Object();
				folder.name = branch[i].name;
				folder.folders = convertBranch(branch[i].folders.values());
				folder.files = branch[i].files.values();
				folder.description = branch[i].description;
				folder.explanation = branch[i].explanation;
				
				// Add new folder object to the array
				folderArray.push(folder);
			}
			
			return folderArray;
		}
		
		/**
		 * Initialize the tree with correct classes, events, etc.
		 * @param $rootElement the DIV element parent of the top level UL
		 * that is used as the root of the tree.
		 */
		function initTree($rootElement) {
			if (settings.tree !== null) {
				setTree($rootElement, new Folder(settings.tree));
				interpretJsonTree(settings.tree, $rootElement, settings);
			}
			initOpButtons($rootElement);
			initTreeClasses($rootElement);
			initExpandCollapse($rootElement);
			initDragDrop($rootElement);
			settings.onInitComplete($rootElement);
		}
		
		/**
		 * Gets the data storage tree from the root element
		 * 
		 * @returns Folder root element of the tree
		 */
		function getTree($rootElement) {
			if (typeof window.fileFolderTree === "undefined") {
				return null;
			}
			return window.fileFolderTree.getItem($rootElement.attr("id"));
		}
		
		/**
		 * Sets the data storage tree to the root element
		 * 
		 * @param tree Folder root element of the new tree
		 * @returns 
		 */
		function setTree($rootElement, tree) {
			if (typeof window.fileFolderTree === "undefined") {
				window.fileFolderTree = new HashTable();
			}
			window.fileFolderTree.setItem($rootElement.attr("id"), tree);
		}
		
		/**
		 * Determines what language to display any interface messages in.  Uses the
		 * browser's language setting.
		 * 
		 * @returns Object defining language text
		 */
		function determineLanguage() {
			var language = window.navigator.userLanguage || window.navigator.language;
			
			// If the language is can't be determined from the browser use US English messages
			if (typeof language == "undefined" || language == "") {
				return oLanguage_en_US;
			}
			
			//  Figure out which language the messages should be in
			if ( language.indexOf("zh-TW") > -1 ) {
				return oLanguage_zh_TW;
			}
			else if ( language.indexOf("zh-CN") > -1 ) {
				return oLanguage_zh_CN;
			}
			else if ( language.indexOf("ko") > -1 ) {
				return oLanguage_ko_KR;
			}
			else if ( language.indexOf("es") > -1 ) {
				return oLanguage_es;
			}
			else {
				return oLanguage_en_US;
			}
		}
		
		/**
		 * Gets the settings assigned to this particular tree
		 * @return Object settings the settings given to this particular tree
		 */
		function getSettings($rootElement) {
			return $rootElement.data("fileFolder-settings");
		}
		
		/**
		 * Sets the settings element for this root element.  These are used
		 * all over for initializing developer-defined settings.
		 * @param $rootElement the jquery root element
		 * @param settings the settings object to store
		 */
		function setSettings($rootElement, settings) {
			$rootElement.data("fileFolder-settings", settings);
		}
		
		function initOpButtons($rootElement) {
			$rootElement.prepend('<div class="fileFolder-buttonBar"></div>');
			$("div.fileFolder-buttonBar").prepend('<input type="button" class="fileFolder-download" value="' + settings.downloadFileLabel + '" />');
			$rootElement.find(".fileFolder-download").click(function() {
				$rootElement.fileFolder("downloadSelected", $rootElement);
			});
			$("div.fileFolder-buttonBar").prepend('<input type="button" class="fileFolder-delete" value="' + settings.deleteItemLabel + '" />');
			$rootElement.find(".fileFolder-delete").click(function() {
				$rootElement.fileFolder("deleteSelected", $rootElement);
			});
			$("div.fileFolder-buttonBar").prepend('<input type="button" class="fileFolder-edit" value="' + settings.editLabel + '" />');
			$rootElement.find(".fileFolder-edit").click(function() {
				$rootElement.fileFolder("editSelected", $rootElement);
			});
			$("div.fileFolder-buttonBar").prepend('<input type="button" class="fileFolder-newFolder" value="' + settings.newFolderLabel + '" />');
			$rootElement.find(".fileFolder-newFolder").click(function() {
				$rootElement.fileFolder("addFolder", $rootElement);
			});
			$("div.fileFolder-buttonBar").prepend('<input type="button" class="fileFolder-upload" value="' + settings.uploadNewLabel + '" />');
			$rootElement.find(".fileFolder-upload").click(function() {
				$rootElement.fileFolder("addFile", $rootElement);
			});
		}
		
		/**
		 * Interprets json from the developer (database) into the HTML needed
		 * to power this whole thing.
		 * 
		 * This function acts recursively to draw the entire folder structure.
		 * 
		 * To accomodate the first step in the recursion, it will check for whether
		 * $folder is a DIV.  If so, it will initialize the top level UL
		 * 
		 * @param json the json tree to draw in this function
		 * @param $folder the jquery reference to the UL of the folder to draw inside
		 */
		function interpretJsonTree(json, $folder, settings) {
			if (!$folder.eq(0).is("UL")) {
				$folder.html("");
				$folder.append("<ul><li><span>" + settings.oLanguage.rootFolderName + "</span><ul></ul></li></ul>");
				$folder = $folder.find("li").children("ul");
			}
			// draw folders first, recursively, then files
			var workingFolder = null;
			for (var i = 0; i < json.folders.length; i++) {
				workingFolder = json.folders[i];
				$folder.append(folderHtml(workingFolder));
				interpretJsonTree(workingFolder, $folder.find("ul").last(), settings);
			}
			
			// draw the files as the exit condition for the recursion
			for (var j = 0; j < json.files.length; j++) {
				$folder.append(fileHtml(json.files[j]));
			}
			

		}
		
		function folderHtml(folderObj) {
			return '<li><span>' + folderObj.name + '</span><ul></ul></li>';
		}
		
		function fileHtml(fileObj) {
			return '<li><span>' + fileObj.name + '</span></li>';
		}
		
		/**
		 * Sets up the tree's visual appearance including adding classes,
		 * collapsing folders, etc.
		 * @param $rootElement the DIV root element of tree
		 */
		function initTreeClasses($rootElement) {
			// the root UL node
			settings = getSettings($rootElement);
			$rootElement.addClass(settings.rootClass);
			$rootElement.children("ul").addClass(settings.folderRootClass);
			var $rootli = $rootElement.children("ul").children("li");
			$rootli.addClass("fileFolder-folder").addClass("fileFolder-rootLi");
			
			$rootli.find("li").each(function() {
				// if we are looking at a folder
				if ($(this).has("ul").length) {
					// give each folder the folder class
					$(this).addClass("fileFolder-folder").addClass("fileFolder-sortable");
					// initially close all folders
					closeFolder($rootElement, $(this));
				}
				else {
					// we're looking at a file
					$(this).addClass(settings.fileClass).addClass("fileFolder-sortable");
				}
			});
		}
		
		function refreshTreeClasses($rootElement) {
			var settings = getSettings($rootElement);
			var $rootli = $rootElement.children("ul").children("li");
			$rootli.find("li").each(function() {
				// if we are looking at a folder
				if ($(this).has("ul").length) {
					// give each folder the folder class
					$(this).addClass("fileFolder-folder").addClass("fileFolder-sortable");
					// initially close all folders
					if (!$(this).hasClass(settings.folderClosedClass) && !$(this).hasClass(settings.folderOpenClass)) {
						closeFolder($rootElement, $(this));
					}
				}
				else {
					// we're looking at a file
					$(this).addClass(settings.fileClass).addClass("fileFolder-sortable");
				}
			});
		}
		
		/**
		 * Handles the click event on ANY li in the tree.
		 * handles opening/closing folders and selecting the element
		 */
		function clickElementCallback($element, event) {
			var $rootElement = $element.closest("div");
			event.stopPropagation();
			select($element);
			var $childUl = $element.children("ul");
			// if we allow the user to close folders manually
			if ($childUl.length > 0) {
				if (settings.allowClose) {
					if ($childUl.is(":visible")) {
						closeFolder($rootElement, $element);
					}
					else {
						openFolder($rootElement, $element);
					}
				}
				else {
					openFolder($rootElement, $element);
				}
			}
		}
		
		/**
		 * Sets up the expand/collapse actions for the folders.
		 * @param $rootElement the DIV root of the tree
		 */
		function initExpandCollapse($rootElement) {
			var captureEvent = "click";
			if (settings.openCloseDoubleclick) {
				captureEvent = "dblclick";
			}
			
			$rootElement.on(captureEvent, "li", function(event){
				clickElementCallback($(this), event);
			});
			
			$rootElement.find("li").eq(0).click();
		}
		
		/**
		 * Initializes the drag/drop funcitonality
		 * @param $rootElement the DIV root of the tree
		 */
		function initDragDrop($rootElement) {
			var $lis = $rootElement.find(".fileFolder-sortable");
			
			$lis.draggable({
				delay: 150,
				revert: "invalid",
				refreshPositions: true,
				scroll: true,
				helper: "clone",
				start : function(event, ui) {
					// Record the starting location
					startingLocation = getNodeLocation(ui.helper);
					
					$rootElement.append(ui.helper);
				}
			});
			
			$(".fileFolder-folder").droppable({
				greedy: true,
				over : function(event, ui) {
					$(this).click();
				},
				drop : function(event, ui) {
					// have to be dropping into a folder
					var settings = getSettings($rootElement);
					var $lastFolder = $(this).children("ul").children(".fileFolder-folder").not(ui.draggable).last();
					if (ui.draggable.hasClass("fileFolder-folder") && $lastFolder.length > 0) {
						$lastFolder.after(ui.draggable);
					}
					else {
						$(this).children("ul").append(ui.draggable);
					}
					
					// Update the tree stucture stored in memory
					var tree = getTree($rootElement);
					var droppedLocation = getNodeLocation($(this));
					
					if ( isFolder(ui.draggable) ) {
						var folder = tree.removeFolder(ui.draggable.children("span").text(), startingLocation, settings);
						tree.addFolder(folder, droppedLocation, settings);
					}
					else {
						var file = tree.removeFile(ui.draggable.children("span").text(), startingLocation, settings);
						tree.addFile(file, droppedLocation, settings);
					}
					
					// Invoke the callback function
					settings.onMoveFinish(ui.draggable, $(this));
				}
			});
		}
		
		function destroyDragDrop($rootElement) {
			$rootElement.find(".fileFolder-sortable").draggable("destroy");
			$(".fileFolder-folder").droppable("destroy");
		}
		
		/**
		 * Closes all folders except the $opened one.  Called when we want to
		 * keep all folders closed except the one currently open.
		 */
		function keepSingleOpen($opened) {
			var $rootElement = $opened.closest("div");
			settings=getSettings($rootElement);
			$rootElement.find("."+settings.folderOpenClass).each(function() {
				if (!$(this).is($opened) && !$(this).is($opened.parents("li"))) {
					closeFolder($rootElement, $(this));
				}
			});
		}
		
		/**
		 * Closes the designated folder
		 * calls onCollapse for the closed folder
		 * @param $rootElement the root DIV element
		 * @param $folder jquery reference to the folder being closed (li)
		 */
		function closeFolder($rootElement, $folder) {
			var settings = getSettings($rootElement);
			$folder.children("ul").hide();
			$folder.removeClass(settings.folderOpenClass);
			$folder.addClass(settings.folderClosedClass);
			settings.onCollapse($folder);
		}
		
		/**
		 * Opens the designated folder
		 * Calls onExpand for the opened folder
		 * @param $rootElement the root DIV element
		 * @param $folder jquery reference to the folder being opened (li)
		 */
		function openFolder($rootElement, $folder) {
			var settings = getSettings($rootElement);
			$folder.children("ul").show();
			$folder.removeClass(settings.folderClosedClass);
			$folder.addClass(settings.folderOpenClass);
			// if we are only allowing one folder open at a time
			if (settings.onlyOneOpen) {
				keepSingleOpen($folder);
			}
			settings.onExpand($folder);
		}
		
		function select($element) {
			var $rootElement = $element.closest("div");
			var selectedClass = getSettings($rootElement).selected;
			getSelected($rootElement).removeClass(selectedClass);
			$element.addClass(selectedClass);
		}
		
		function getSelected($rootElement) {
			return $rootElement.find("."+getSettings($rootElement).selected);
		}
		
		/**
		 * Gets the location of the specified node in URL format
		 */
		function getNodeLocation($node) {
			if ($node.hasClass("fileFolder-rootLi")) {
				return "";
			}
			var locationArr = [];
			locationArr.unshift($node.children("span").text().trim());
			var $parents = $node.parents("li");
			for (var i = 0; i < $parents.length; i++) {
				$node = $parents.eq(i);
				if ($node.hasClass("fileFolder-root") || $node.hasClass("fileFolder-rootLi") || $node.is("div")) {
					break;
				}
				locationArr.unshift($node.children("span").text().trim());
			}
			return locationArr.join("/");
		}
		
		function isFolder($node) {
			return $node.children("ul").length > 0;
		}
		
		function uploadFile(file) {
			// create iframe
		}
		
		// --------------------------------------------------------------
		// Method calling logic
		if ( methods[method] ) {
			return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
		} else if ( typeof method === 'object' || ! method ) {
			return methods.init.apply( this, arguments );
		} else {
			$.error( 'Method ' +  method + ' does not exist on jquery.fileFolder' );
		}
	};
	
	// -------------------------------------------------------------------
	// defaults and callbacks go here
	$.fn.fileFolder.defaults = {
		tree : null,
		openCloseDoubleclick : false,
		verifyDelete: true,
		onlyOneOpen : true,
		allowClose : false,
		folderOpenClass : "fileFolder-open",
		folderClosedClass : "fileFolder-closed",
		folderRootClass : "fileFolder-root",
		fileClass : "fileFolder-file",
		rootClass : "fileFolder-rootDiv",
		selected : "fileFolder-selected",
		newFolderLabel : "New Folder",
		editLabel : "Edit",
		deleteItemLabel : "Delete",
		uploadNewLabel : "New File",
		downloadFileLabel : "Download",
		oLanguage : "undefined",
		fileUpload : {
			targetUrl : "",
			fieldNames : {
				id : "id",
				name : "name",
				author : "author",
				description: "description",
				type : "type",
				file: "file",
				url : "url",
				pubMedId : "pubMedId",
				fileName : "fileName"
			}
		},
		
		onMoveFinish : function($moved, $target) {},
		onExpand : function($element) {},
		onCollapse : function($element) {},
		onInitComplete : function($rootElement) {},
		onEdit : function($originalElement, $element){},
		onDeleteFolder : function(folderObj) {},
		onDeleteFile : function(fileObj){},
		onEditFile : function($file, editFormData){},
		onAddFolder : function(folderObj){},
		onAddFile : function($file){},
		onFileUpload : function(file, success){},
		onDownload : function(files){}
	};
  
})( jQuery );
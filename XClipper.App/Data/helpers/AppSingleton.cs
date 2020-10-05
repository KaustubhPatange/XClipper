using ClipboardManager.models;
using System.Collections.Generic;
using System.Linq;
using static Components.MainHelper;
using static Components.DefaultSettings;
using static Components.TableHelper;
using static Components.Constants;
using System.Threading.Tasks;
using Autofac;
using static WK.Libraries.SharpClipboardNS.SharpClipboard;
using System;
using System.Diagnostics;
using System.Drawing.Printing;
using System.Text.RegularExpressions;
using System.IO;
using RestSharp.Extensions;

#nullable enable

namespace Components.viewModels
{
    public class AppSingleton
    {
        #region Variable Declaration

        private int TotalClips = 0;
        private int TotalPage = 0;
        private int Page = 1;

        private IClipBinder Binder;
        private IDatabase<TableCopy> dataDB;
        // public SQLiteConnection dataDB;
        private static AppSingleton Instance = null;

        #endregion


        #region Singleton

        public static AppSingleton GetInstance
        {
            get
            {
                if (Instance == null)
                    Instance = new AppSingleton();
                return Instance;
            }
        }

        private AppSingleton()
        {
            dataDB = AppModule.Container.Resolve<IDatabase<TableCopy>>();
        }

        #endregion


        #region Methods

        public void Close() => dataDB.CloseConnection();
        public void Init() => dataDB.Initialize();

        public void SetBinder(IClipBinder Binder)
        {
            this.Binder = Binder;
        }


        #endregion


        #region IClipBinder Invokes

        /// <summary>
        /// This will make an exit request to main window to close the window.
        /// </summary>
        public void MakeExitRequest()
        {
            Binder.OnExitRequest();
        }
        public void DeleteData(List<TableCopy> models)
        {
            DeleteClipData(models);
            //models.ForEach((model) => { DeleteClipData(model); });
            Binder.OnModelDeleted(ClipData);
        }
        public void TogglePin(TableCopy model)
        {
            model.IsPinned = !model.IsPinned;
            dataDB.Query("update TableCopy set IsPinned = ? where Id = ?", model.IsPinned, model.Id);
            Binder.OnModelDeleted(ClipData);
        }
        public void UpdateData(TableCopy model)
        {
            // We will encrypt data here coz... why not...
            dataDB.Query("update TableCopy set Text = ?, LongText = ?, RawText = ? where Id = ?", model.Text, model.LongText, model.RawText, model.Id);
            Binder.OnPopupTextEdited(ClipData);
        }

        public void UpdateLastUsedTime(TableCopy model)
        {
            dataDB.Query("update TableCopy set LastUsedDateTime = ? where Id = ?", model.LastUsedDateTime, model.Id);
        }

        #endregion


        #region Data Filtering

        public void SetFilterText(string Text) => Binder.OnFilterTextEdit(Text);

        public List<TableCopy> FilterTextLengthDesc()
        {
            var data = dataDB.GetAllData();
            return data.Where(x => x.ContentType == ContentType.Text).OrderByDescending(x => x.RawText.Length).Take(TruncateList).ToList();
        }

        public List<TableCopy> FilterTextLengthAsc()
        {
            var data = dataDB.GetAllData();
            return data.Where(x => x.ContentType == ContentType.Text).OrderBy(x => x.RawText.Length).Take(TruncateList).ToList();
        }
        public List<TableCopy> FilterOldest() => dataDB.GetAllData().Take(TruncateList).ToList();
        public List<TableCopy> FilterNewest() => dataDB.GetAllData().Reverse<TableCopy>().Take(TruncateList).ToList();
        public List<TableCopy> FilterData(string text) => dataDB.GetAllData().Where(s => s.Text.ToLower().Contains(text.ToLower())).Reverse().Take(TruncateList).ToList();
        public List<TableCopy> FilterContentType(ContentType type) => dataDB.GetAllData().Where(s => s.ContentType == type).Reverse().Take(TruncateList).ToList();
        public List<TableCopy> FilterPinned() => dataDB.GetAllData().Where(s => s.IsPinned).Reverse().Take(TruncateList).ToList();
        public List<TableCopy> FilterUnpinned() => dataDB.GetAllData().Where(s => !s.IsPinned).Reverse().Take(TruncateList).ToList();

        #endregion


        #region Data Obtaining Methods

        public List<TableCopy> GetAllData() => dataDB.GetAllData();

        public List<TableCopy> ClipData
        {
            get
            {
                var pinnedItems = dataDB.GetAllData().Where(x => x.IsPinned).Reverse();
                var normalItems = dataDB.GetAllData().Where(x => !x.IsPinned)
                    .OrderByDescending(x => ParseDateTimeText(x.LastUsedDateTime));

                TotalClips = pinnedItems.Count() + normalItems.Count();
                TotalPage = (TotalClips / TruncateList) + ((TotalClips % TruncateList != 0) ? 1 : 0);

                return pinnedItems.Concat(normalItems).ToList();
            }
        }

        #endregion


        #region SQLite Methods

        #region Insert Content

        public void InsertAll(List<TableCopy> models) => dataDB.InsertAll(models);

        public void InsertContent(TableCopy model, bool pushToDatabase = true)
        {
            // This will check if same clip text is saved again
            var list = dataDB.GetAllData().OrderByDescending(s => ParseDateTimeText(s.LastUsedDateTime)).ToList();
            foreach (var c in list)
            {
                if (c.ContentType == model.ContentType)
                {
                    switch (model.ContentType)
                    {
                        case ContentType.Text:
                            if (model.Text == c.Text) return;
                            break;
                        case ContentType.Image:
                            if (model.ImagePath == c.ImagePath) return;
                            break;
                        case ContentType.Files:
                            if (model.LongText == c.LongText) return;
                            break;
                    }
                }
            }

            // Implementation of setting TotalClipLength 
            if (list.Count >= TotalClipLength)
            {
                dataDB.Delete(list[list.Count - 1]);
            }

            dataDB.Insert(model);

            if (pushToDatabase)
            {
                FirebaseSingleton.GetInstance.AddClip(model.ContentType == ContentType.Text ? model.RawText : null).RunAsync();
                FirebaseSingleton.GetInstance.AddImage(model.ContentType == ContentType.Image ? model.ImagePath : null).RunAsync();
            }
        }

        public void InsertTextClipNoUpdate(string UnEncryptedText)
        {
            InsertContent(CreateTable(UnEncryptedText, ContentTypes.Text), false);
        }

        #endregion

        #region UpdateData

        /// <summary>
        /// This will compare the given text data with the local database table items.
        /// If not exist such item, it will insert the data.
        /// </summary>
        /// <param name="EncryptedText">Encrypted Text Data coming straight away from online database.</param>
        /// <param name="invokeOnInserted">Data will be an unencrypted clip data.</param>
        public void CheckDataAndUpdate(string? EncryptedText, Action<string>? invokeOnInserted = null)
        {
            if (EncryptedText == null) return;

            var decryptedText = EncryptedText.DecryptBase64(DatabaseEncryptPassword);

            if (Regex.IsMatch(decryptedText, PATH_CLIP_IMAGE_DATA))
            {
                UpdateDataForImage(decryptedText, invokeOnInserted);
                return;
            }

            bool dataExist = false;
            dataExist = dataDB.GetAllData().Exists(c => c.RawText == decryptedText);
            if (!dataExist)
            {
                // Insert this data without updating online database.
                Debug.WriteLine("Inserted Data");
                InsertTextClipNoUpdate(decryptedText);
                invokeOnInserted?.Invoke(decryptedText);
            }
        }

        /// <summary>
        /// This will add Image related queries to the local database coming from server.<br/><br/>
        /// 
        /// It also serves an additional purpose, naturally we cannot directly add image to <br/>
        /// server because the image alt in markdown must match the local database model fileName. <br/><br/>
        /// 
        /// That's why this can use to find the image markdown from the server, then down the image <br/>
        /// and update it to local server. <br/>
        /// </summary>
        /// <param name="unEncryptedText"></param>
        /// <param name="invokeOnInserted"></param>
        public async void UpdateDataForImage(string unEncryptedText, Action<string>? invokeOnInserted = null)
        {
            var match = Regex.Match(unEncryptedText, PATH_CLIP_IMAGE_DATA);
            var fileName = match.Groups[2].Value.UrlDecode();
            var imageUri = match.Groups[5].Value;

            bool dataExist = dataDB.GetAllData().Exists(c => c?.ImagePath?.EndsWith(fileName) ?? false);
            if (!dataExist)
            {
                var filePath = Path.Combine(ImageFolder, fileName);
                await DownloadFile(imageUri, filePath).ConfigureAwait(false);
                InsertContent(CreateTable(filePath, ContentTypes.Image), false);
                invokeOnInserted?.Invoke(unEncryptedText); 
                // TODO: show some valid response when image is added to database. Currently it shows notify message with text.
            }
        }

        /// <summary>
        /// Method will modify existing data locally as well as from Firebase.
        /// </summary>
        public void ModifyData(string oldData, TableCopy newData)
        {
            UpdateData(newData);
            if (newData?.ContentType == ContentType.Text)
                FirebaseSingleton.GetInstance.UpdateData(oldData, newData.RawText).RunAsync();
        }

        #endregion

        #region DeleteData

        /// <summary>
        /// This will delete the clip based on matched RawText from local database.
        /// </summary>
        /// <param name="data"></param>
        /// <returns></returns>
        public bool DeleteClipData(string? data)
        {
            TableCopy item = dataDB.GetAllData().Find(c => c.RawText == data);

            if (item != null) DeleteClipData(item, false);
            return item != null;
        }

        /// <summary>
        /// This will delete item locally as well as from Firebase.
        /// </summary>
        /// <param name="model"></param>
        /// <param name="fromFirebase"></param>
        public void DeleteClipData(TableCopy model, bool fromFirebase = true)
        {
            dataDB.Delete(model);

            if (fromFirebase)
            {
                switch (model?.ContentType)
                {
                    case ContentType.Text:
                        FirebaseSingleton.GetInstance.RemoveClip(model.RawText).RunAsync();
                        break;
                    case ContentType.Image:
                        FirebaseSingleton.GetInstance.RemoveImage(Path.GetFileName(model.ImagePath)).RunAsync();
                        break;
                }
            }
        }

        /// <summary>
        /// This will delete list of items locally as well as from Firebase.
        /// </summary>
        /// <param name="models"></param>
        /// <param name="fromFirebase"></param>
        public void DeleteClipData(List<TableCopy> models, bool fromFirebase = true)
        {
            dataDB.Delete(models);

            if (fromFirebase)
            {
                FirebaseSingleton.GetInstance.RemoveClip(models.Select(c => c.RawText).OfType<string>().ToList()).RunAsync();
                FirebaseSingleton.GetInstance.RemoveImageList(models.Select(c => c.ImagePath).OfType<string>()
                    .Select(c => Path.GetFileName(c)).ToList()).RunAsync();
            }
        }

        /// <summary>
        /// This will remove all data from database.
        /// </summary>
        /// <param name="fromFirebase"></param>
        public void DeleteAllData(bool fromFirebase = true)
        {
            dataDB.ClearAll<TableCopy>();

            if (fromFirebase)
                FirebaseSingleton.GetInstance.RemoveAllClip().RunAsync();
        }

        #endregion

        #endregion    
    }
}

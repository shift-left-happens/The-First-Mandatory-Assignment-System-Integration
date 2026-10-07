using CoreWCF;
using CoreWCF.Configuration;
using CoreWCF.Description;
using LibrarySoap;

var builder = WebApplication.CreateBuilder(args);

// CoreWCF: the SOAP runtime + WSDL generation.
builder.Services.AddServiceModelServices();
builder.Services.AddServiceModelMetadata();

// Our own classes.
builder.Services.AddSingleton<Db>();
builder.Services.AddSingleton<BookRepository>();
builder.Services.AddSingleton<AuthorRepository>();
builder.Services.AddSingleton<PublishingCompanyRepository>();
builder.Services.AddTransient<LibraryService>();

var app = builder.Build();

app.UseServiceModel(soap =>
{
    soap.AddService<LibraryService>();
    // BasicHttpBinding = SOAP 1.1 over plain HTTP (the most widely supported flavour of SOAP).
    soap.AddServiceEndpoint<LibraryService, ILibraryService>(new BasicHttpBinding(), "/LibraryService.svc");
});

// Publish the WSDL at /LibraryService.svc?wsdl (and ?singleWsdl).
app.Services.GetRequiredService<ServiceMetadataBehavior>().HttpGetEnabled = true;

app.Run();

import os
import sys
import subprocess

def install_pywin32():
    try:
        import win32com.client
    except ImportError:
        print("Installing pywin32 to automate Microsoft Word...")
        try:
            subprocess.check_call([sys.executable, "-m", "pip", "install", "pywin32"])
            print("pywin32 installed successfully!")
        except Exception as e:
            print(f"Failed to install pywin32 automatically via pip: {e}")
            print("Please run: pip install pywin32")
            sys.exit(1)

def docx_to_pdf(docx_path, pdf_path):
    import win32com.client
    
    # Get absolute paths
    docx_path = os.path.abspath(docx_path)
    pdf_path = os.path.abspath(pdf_path)
    
    if not os.path.exists(docx_path):
        print(f"Error: Word file not found at {docx_path}")
        sys.exit(1)
        
    print(f"Opening Microsoft Word to update fields and convert to PDF...")
    word = None
    try:
        # Initialize Word application
        word = win32com.client.Dispatch("Word.Application")
        word.Visible = False
        
        # Open document
        doc = word.Documents.Open(docx_path)
        
        # Update all Tables of Contents silently
        print("Updating Table of Contents...")
        for toc in doc.TablesOfContents:
            toc.Update()
            
        # Save as PDF (wdFormatPDF = 17)
        print(f"Saving PDF to: {pdf_path}")
        doc.SaveAs(pdf_path, FileFormat=17)
        doc.Close()
        print("PDF generated successfully with updated Table of Contents!")
    except Exception as e:
        print(f"Error during Microsoft Word PDF conversion: {e}")
        sys.exit(1)
    finally:
        if word is not None:
            try:
                word.Quit()
            except:
                pass

if __name__ == "__main__":
    install_pywin32()
    
    cwd = os.path.dirname(os.path.abspath(__file__))
    docx_default = os.path.join(cwd, "BaoCao_QuanLyPhongTro.docx")
    pdf_default = os.path.join(cwd, "BaoCao_QuanLyPhongTro.pdf")
    
    docx_file = sys.argv[1] if len(sys.argv) > 1 else docx_default
    pdf_file = sys.argv[2] if len(sys.argv) > 2 else pdf_default
    
    docx_to_pdf(docx_file, pdf_file)

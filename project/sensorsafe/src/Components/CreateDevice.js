import React, {useState, useEffect} from 'react';
import '../Css/CreateDevice.css';
import { useNavigate } from 'react-router-dom';
import Toastify from './Toastify';

function AddDevice() {
    const navigate = useNavigate();

    const [selectedCategory, setSelectedCategory] = useState('');
    const [selectedType, setSelectedType] = useState('');

    const handleCategoryClick = (category) => {
        setSelectedCategory(category);
    };
    
    const handleTypeClick = (type) => {
      setSelectedType(type);
    };
    
    const handleAddDevice = async (e) => {

      e.preventDefault();
  
      const formData = new FormData(e.currentTarget);
     

      if (selectedCategory === '') {
        Toastify.warning('Please select a type');
        return;
      }

      if (selectedCategory !== 'OTHERS' && selectedType === '') {
        Toastify.warning('Please select a type');
        return;
      }

      
      if (selectedCategory === 'OTHERS') {
        console.log('OTHERS');
        try {
          const response = await fetch('http://localhost:8080/api/devices/device/create', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Authorization':'Bearer ' + sessionStorage.getItem('Token:'),
              
            },
            body: JSON.stringify({
              name: formData.get('name'),
              category: selectedCategory,
            }),
          });

          
          if (!response.ok) {
            throw new Error('Network response was not ok');
          }
    
          const data = await response.json();
    
          if (data && data.message === 'Device created successfully') {
            console.log(response);

            // Registration was successful
            Toastify.success('Device created successfully');

            navigate('/devices');
          } else {
            Toastify.error('Device creation failed. Error: ' + data.message);
          }
          
        } catch (error) {
          Toastify.info('Error connecting to server');
          console.log('Error in /device post', error);
        }
      }

      else {
        console.log('sensor');
    
        try {
          const response = await fetch('http://localhost:8080/api/devices/sensor/create', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Authorization':'Bearer ' + sessionStorage.getItem('Token:'),
              
            },
            body: JSON.stringify({
              name: formData.get('name'),
              category: selectedCategory,
              sensorStatus: selectedType === 'On' ? true : false
            }),
          });

          console.log(response);
          
          if (!response.ok) {
            throw new Error('Network response was not ok');
          }
    
          const data = await response.json();
    
          if (data && data.message === 'Sensor created successfully') {

            // Registration was successful
            Toastify.success('Sensor created successfully');

            navigate('/devices');
          } else {
            // Registration failed, handle accordingly
            Toastify.error('Sensor creation failed. Error: ' + data.message);
          }
          
        } catch (error) {
          Toastify.info('Error connecting to server');
          console.log('Error in /sensor post', error);
        }
      }
    };

    return (
        <div className='additem-page'>
        <div className='additem-modal'>
          <h3>Create Device</h3>
          <form onSubmit={handleAddDevice} encType="multipart/form-data">
            <div className='form-group-item'>
              <label htmlFor='name'>Name:</label>
              <input
                type='text'
                name='name'
                id='name'
                required
              />
            </div>
            <div className='category-selection'>
                <h2>Select Category</h2>
                <div className='category-options'>
                    <span className={`category-option ${selectedCategory === 'HUMIDITY' ? 'active' : ''}`} onClick={() => handleCategoryClick('HUMIDITY')}>HUMIDITY</span>
                    <span className={`category-option ${selectedCategory === 'TEMPERATURE' ? 'active' : ''}`} onClick={() => handleCategoryClick('TEMPERATURE')}>TEMPERATURE</span>
                    <span className={`category-option ${selectedCategory === 'SMOKE' ? 'active' : ''}`} onClick={() => handleCategoryClick('SMOKE')}>SMOKE</span>
                    <span className={`category-option ${selectedCategory === 'OTHERS' ? 'active' : ''}`} onClick={() => handleCategoryClick('OTHERS')}>OTHERS</span>
                </div>
            </div>
            {selectedCategory === 'OTHERS' ? ( 
                <div></div>
               ) : (
                <div className='category-selection'>
                <h2>Select Sensor Status</h2>
                <div className='category-options'>
                        <span className={`type-option-green ${selectedType === 'On' ? 'active' : ''}`} onClick={() => handleTypeClick('On')}>On</span>
                        <span className={`type-option-red ${selectedType === 'Off' ? 'active' : ''}`} onClick={() => handleTypeClick('Off')}>Off</span>
                    </div>
                </div>
            
            )}
            <button className='btn edit-button additem' type='submit'><i className="animation"></i>Add device<i className="animation"></i></button>
          </form>
        </div>
        <div className="info-saved-modal" id="success-modal" style={{ display: 'none' }}>
              <p>Device added</p>
          </div>
      </div>
    );
}

export default AddDevice;

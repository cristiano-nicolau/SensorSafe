import React, {useState, useEffect} from 'react';
import '../Css/CreateRoom.css';
import { useNavigate } from 'react-router-dom';
import Toastify from './Toastify';


function CreateRoom() {
    const navigate = useNavigate();

    const [selectedCategory, setSelectedCategory] = useState('');

    const handleCategoryClick = (category) => {
      setSelectedCategory(category);
      };
    
    const handleAddItem = async (e) => {
      e.preventDefault();
    
      const formData = new FormData(e.currentTarget);
    
      try {
        const response = await fetch('http://localhost:8080/sensorsafe/rooms', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            roomName: formData.get('roomName'),
            automatized: selectedCategory
          }),
        });

        
        
        if (!response.ok) {
          console.log(response);
          Toastify.warning('Room creation failed, try again');
          throw new Error('Network response was not ok');
        }
  
        const data = await response.json();
  
        if (data && data.message === 'Room as been successfully created') {
          console.log(response);

          // Registration was successful
          Toastify.success('Room as been created successfully');
          sessionStorage.setItem('Bearer ', data.token);

          navigate('/rooms');
        } else {
          console.log(response);
          // Registration failed, handle accordingly
          Toastify.error('Room creation failed, try again. Error: ' + data.message);
        }
        
      } catch (error) {
        console.log(error);
        Toastify.info('Error connecting to server');
        console.log('Error in auth/signup post', error);
      }
    };


    return (
        <div className='additem-page-room'>
        <div className='additem-modal-room'>
          <h3>Create Room</h3>
          <form onSubmit={handleAddItem} encType="multipart/form-data">
            <div className='form-group-item-room'>
              <label htmlFor='name'>Name:</label>
              <input
                type='text'
                id='roomName'
                name='roomName'
                required
              />
            </div>
            <div className='category-selection-room'>
                <h2>Automatized</h2>
                <div className='category-options-room'>
                  <span className={`category-option-room ${selectedCategory === true ? 'active' : ''}`} onClick={() => handleCategoryClick(true)}>True</span>
                  <span className={`category-option-room ${selectedCategory === false ? 'active' : ''}`} onClick={() => handleCategoryClick(false)}>False</span>
              </div>

            </div>
            <button className='btn edit-button addroom' type='submit'><i className="animation"></i>Add room<i className="animation"></i></button>
          </form>
        </div>
        <div className="info-saved-modal" id="success-modal" style={{ display: 'none' }}>
              <p>Room added</p>
          </div>
      </div>
    );
}

export default CreateRoom;
